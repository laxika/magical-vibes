package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.cards.k.KarlachRagingTiefling;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.SpecializeKarlachEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Applies Karlach's five specialized faces from the battlefield or graveyard. */
@Component
@RequiredArgsConstructor
public class SpecializeKarlachEffectHandler implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final PermanentRemovalService permanentRemovalService;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final GraveyardReturnSupport graveyardReturnSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SpecializeKarlachEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        CardColor color = ((SpecializeKarlachEffect) effect).color();
        Permanent source = entry.getSourcePermanentId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        Card sourceCard = source == null
                ? gameQueryService.findCardInGraveyardById(gameData, entry.getCard().getId())
                : source.getCard();
        if (sourceCard == null || !"Karlach, Raging Tiefling".equals(sourceCard.getName())) {
            return;
        }

        Card specialized = sourceCard.createRuntimeCopy();
        specialized.clearRulesTextAndAbilities();
        KarlachRagingTiefling.setSpecializedBaseCharacteristics(specialized);
        specialized.setPower(4);
        specialized.setToughness(4);
        setFaceCharacteristics(specialized, color);
        specialized.addEffect(EffectSlot.STATIC, new CantBlockEffect());

        TargetFilter targetFilter = KarlachRagingTiefling.specializedTargetFilter(color);
        if (targetFilter != null) {
            specialized.target(targetFilter);
        }

        UUID sourcePermanentId;
        UUID controllerId = entry.getControllerId();
        if (source != null) {
            source.exchangeCard(specialized);
            sourcePermanentId = source.getId();
        } else {
            UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, sourceCard.getId());
            if (ownerId == null
                    || graveyardReturnSupport.isCardBlockedFromEnteringFromZone(
                    gameData, sourceCard, Zone.GRAVEYARD)) {
                return;
            }

            permanentRemovalService.removeCardFromGraveyardById(gameData, sourceCard.getId());
            Permanent returned = new Permanent(specialized);
            returned.setEnteredFromGraveyardOwnerId(ownerId);
            battlefieldEntryService.putPermanentOntoBattlefield(
                    gameData, ownerId, returned,
                    battlefieldEntryService.snapshotEnterTappedTypes(gameData));
            graveyardReturnSupport.handleCreatureEtbAndLegendRule(gameData, ownerId, returned, specialized);
            sourcePermanentId = returned.getId();
            controllerId = ownerId;
            gameLogService.append(gameData, GameLog.textCardText(
                    gameData.playerIdToName.get(ownerId) + " returns ", specialized,
                    " to the battlefield as " + specialized.getName() + "."));
        }

        enqueueSpecializationTrigger(gameData, specialized, sourcePermanentId, targetFilter, controllerId,
                KarlachRagingTiefling.specializedTrigger(color));
    }

    private void enqueueSpecializationTrigger(GameData gameData, Card sourceCard,
                                              UUID sourcePermanentId, TargetFilter targetFilter,
                                              UUID controllerId, CardEffect triggerEffect) {
        StackEntry trigger = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                controllerId,
                sourceCard.getName() + "'s ability",
                List.of(triggerEffect),
                0,
                sourcePermanentId);
        trigger.setTargetFilter(targetFilter);
        trigger.setNonTargeting(targetFilter == null);
        gameData.enqueueTrigger(trigger);
    }

    private void setFaceCharacteristics(Card card, CardColor color) {
        switch (color) {
            case BLACK -> {
                card.setName("Karlach, Tiefling Punisher");
                card.setManaCost("{1}{B}{R}");
                card.setColors(List.of(CardColor.BLACK, CardColor.RED));
                card.setColorIdentity(List.of(CardColor.BLACK, CardColor.RED));
                card.setColor(CardColor.BLACK);
                card.setCardText("First strike\nHaste\nWhen this card specializes from your graveyard, return it from your graveyard to the battlefield. It perpetually gains \"This creature can't block.\"\nWhen this card specializes from any zone, you may sacrifice a creature. If you do, you draw two cards and each opponent loses 2 life.");
            }
            case BLUE -> {
                card.setName("Karlach, Tiefling Spellrager");
                card.setManaCost("{1}{U}{R}");
                card.setColors(List.of(CardColor.BLUE, CardColor.RED));
                card.setColorIdentity(List.of(CardColor.BLUE, CardColor.RED));
                card.setColor(CardColor.BLUE);
                card.setCardText("First strike\nHaste\nWhen this card specializes from your graveyard, return it from your graveyard to the battlefield. It perpetually gains \"This creature can't block.\"\nWhen this card specializes from any zone, seek an instant or sorcery card with mana value 3 or less. Until end of turn, you may cast that card without paying its mana cost.");
            }
            case RED -> {
                card.setName("Karlach, Tiefling Berserker");
                card.setManaCost("{1}{R}{R}");
                card.setCardText("First strike\nHaste\nWhen this card specializes from your graveyard, return it from your graveyard to the battlefield. It perpetually gains \"This creature can't block.\"\nWhen this card specializes from any zone, target creature an opponent controls can't block this turn.");
            }
            case GREEN -> {
                card.setName("Karlach, Tiefling Guardian");
                card.setManaCost("{1}{R}{G}");
                card.setColors(List.of(CardColor.GREEN, CardColor.RED));
                card.setColorIdentity(List.of(CardColor.GREEN, CardColor.RED));
                card.setColor(CardColor.GREEN);
                card.setCardText("First strike\nHaste\nWhen this card specializes from your graveyard, return it from your graveyard to the battlefield. It perpetually gains \"This creature can't block.\"\nWhen this card specializes from any zone, another target creature you control gets +4/+4 until end of turn.");
            }
            case WHITE -> {
                card.setName("Karlach, Tiefling Zealot");
                card.setManaCost("{1}{R}{W}");
                card.setColors(List.of(CardColor.RED, CardColor.WHITE));
                card.setColorIdentity(List.of(CardColor.RED, CardColor.WHITE));
                card.setColor(CardColor.WHITE);
                card.setCardText("First strike\nHaste\nWhen this card specializes from your graveyard, return it from your graveyard to the battlefield. It perpetually gains \"This creature can't block.\"\nWhen this card specializes from any zone, create a 2/2 white Knight creature token. Creatures you control get +1/+1 and gain haste until end of turn.");
            }
        }
    }
}
