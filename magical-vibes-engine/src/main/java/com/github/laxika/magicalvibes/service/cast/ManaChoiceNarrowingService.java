package com.github.laxika.magicalvibes.service.cast;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPaymentIntent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.VirtualManaPool;
import com.github.laxika.magicalvibes.service.GameActionAvailabilityService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Greys out the mana colours that would strand the payment a player is in the middle of.
 *
 * <p>An "add one mana of any colour" source (Birds of Paradise) prompts for a colour. When the
 * player is only tapping it because they clicked a spell first, most of those colours are dead
 * ends: with three Plains, a Forest and a Bird facing a {3}{G}{R} cast, only red keeps the spell
 * payable, because the Bird is the sole red source. Offering all five invites a misclick that
 * wastes the source.
 *
 * <p>The check is exact rather than heuristic: the source is already tapped by the time its colour
 * is chosen, so {@link PotentialManaService#buildVirtualManaPool} at that moment is precisely
 * "everything already floating plus everything still tappable, excluding this source". A colour is
 * viable when adding it to that pool leaves the intended cost payable, which is decided by the same
 * {@link GameActionAvailabilityService#isCardPlayable} / {@link ManaCost#canPay} used for
 * playability, so cost reduction, alternative costs and mutually-exclusive dual sources are all
 * priced correctly and no mana solver is duplicated.
 *
 * <p>The result is advisory. It never removes an option and the answer handler never rejects one —
 * a greyed colour stays legal, and the whole narrowing is skipped whenever it cannot be trusted:
 * restricted mana (flashback-only, creature-spells-only, …), an intent that no longer matches the
 * board, or a computation in which every colour or no colour looks dead.
 */
@Service
@RequiredArgsConstructor
public class ManaChoiceNarrowingService {

    private final GameQueryService gameQueryService;
    private final PotentialManaService potentialManaService;
    private final GameActionAvailabilityService gameActionAvailabilityService;

    /**
     * Refines the active colour prompt in place when it is an unrestricted mana-colour choice
     * raised by a source the player tapped to pay {@code intent}. A no-op in every other case.
     */
    public void narrowActiveManaColorChoice(GameData gameData, UUID playerId, ManaPaymentIntent intent) {
        if (intent == null) {
            return;
        }
        PendingInteraction.ColorChoice choice =
                gameData.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        if (choice == null
                || !playerId.equals(choice.playerId())
                || !choice.disabledOptions().isEmpty()
                || !(choice.context() instanceof ChoiceContext.ManaColorChoice manaChoice)
                || !isUnrestricted(manaChoice)) {
            return;
        }

        List<String> disabled = disabledColors(gameData, playerId, choice.options(), manaChoice.amount(), intent);
        if (disabled.isEmpty()) {
            return;
        }
        gameData.interaction.replaceActiveInteraction(choice.withDisabledOptions(disabled));
    }

    /**
     * The offered colours that would leave {@code intent} unpayable. Empty (narrow nothing) when
     * the intent cannot be resolved, when no colour helps, or when every colour does.
     */
    private List<String> disabledColors(GameData gameData, UUID playerId, List<String> options,
                                        int amount, ManaPaymentIntent intent) {
        List<String> disabled = new ArrayList<>();
        int viable = 0;
        for (String option : options) {
            ManaColor color = parseColor(option);
            if (color == null) {
                // An option this service does not understand is never greyed out.
                return List.of();
            }
            Boolean payable = payableWith(gameData, playerId, color, Math.max(1, amount), intent);
            if (payable == null) {
                return List.of();
            }
            if (payable) {
                viable++;
            } else {
                disabled.add(option);
            }
        }
        // No colour rescues the payment (the player is short elsewhere, or the intent is stale):
        // greying everything would just look broken, so leave the prompt untouched.
        return viable == 0 ? List.of() : disabled;
    }

    /**
     * Whether the intended cost is payable once {@code amount} mana of {@code color} joins the pool,
     * or {@code null} when the intent no longer describes anything on the board.
     */
    private Boolean payableWith(GameData gameData, UUID playerId, ManaColor color, int amount,
                                ManaPaymentIntent intent) {
        VirtualManaPool pool = potentialManaService.buildVirtualManaPool(gameData, playerId);
        pool.add(color, amount);

        if (intent.isCast()) {
            List<Card> hand = gameData.playerHands.get(playerId);
            int index = intent.handCardIndex();
            if (hand == null || index < 0 || index >= hand.size()) {
                return null;
            }
            return gameActionAvailabilityService.isCardPlayable(
                    gameData, playerId, hand.get(index), pool, intent.announcedX());
        }
        if (!intent.isAbility()) {
            return null;
        }
        Permanent source = gameQueryService.findPermanentById(gameData, intent.abilityPermanentId());
        if (source == null) {
            return null;
        }
        List<ActivatedAbility> abilities = source.getCard().getActivatedAbilities();
        int index = intent.abilityIndex();
        if (index < 0 || index >= abilities.size()) {
            return null;
        }
        String abilityCost = abilities.get(index).getManaCost();
        if (abilityCost == null) {
            return null;
        }
        // Mirrors the affordability call in GameActionAvailabilityService.getPotentialPayableAbilityIndices
        // so an ability payment is priced exactly as its "potentially payable" highlight was.
        boolean artifactContext = gameQueryService.isArtifact(source);
        boolean myrContext = source.getCard().getSubtypes().contains(CardSubtype.MYR);
        Set<CardSubtype> subtypeContext = new HashSet<>(source.getCard().getSubtypes());
        subtypeContext.addAll(source.getTransientSubtypes());
        subtypeContext.addAll(source.getGrantedSubtypes());
        return new ManaCost(abilityCost).canPay(pool, 0, artifactContext, myrContext, false, false,
                false, null, subtypeContext, false, artifactContext);
    }

    /**
     * True when the produced mana can pay for anything. Mana that is spendable only on some spells
     * (flashback, creature spells, a subtype's abilities, a fixed colour menu) needs the restriction
     * applied before "would this cast still be payable" means anything, so those prompts are left alone.
     */
    private boolean isUnrestricted(ChoiceContext.ManaColorChoice manaChoice) {
        return manaChoice.restrictedToCreatureSubtype() == null
                && !manaChoice.flashbackOnly()
                && !manaChoice.instantSorceryOnly()
                && !manaChoice.spellOrAbilitySubtype()
                && !manaChoice.creatureSpellOnly()
                && !manaChoice.artifactSpellOrAbilityOnly()
                && manaChoice.fixedColorOptions() == null;
    }

    private ManaColor parseColor(String option) {
        for (ManaColor color : ManaColor.values()) {
            if (color.name().equals(option)) {
                return color;
            }
        }
        return null;
    }
}
