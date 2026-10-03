package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DetainmentSpell;
import com.github.laxika.magicalvibes.cards.e.EternalThirst;
import com.github.laxika.magicalvibes.cards.f.FleshToDust;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BroodKeeper.class, EternalThirst.class, RuneclawBear.class, FleshToDust.class, DetainmentSpell.class})
class BroodKeeperTest extends BaseCardTest {

    @Test
    @DisplayName("An Aura becoming attached to Brood Keeper creates a 2/2 red flying Dragon token")
    void auraAttachCreatesDragonToken() {
        Permanent keeper = addCreatureReady(player1, new BroodKeeper());

        enchantWithEternalThirst(player1, keeper);

        List<Permanent> tokens = findPermanents(player1, "Dragon");
        assertThat(tokens).hasSize(1);
        Permanent token = tokens.getFirst();
        assertThat(token.getCard().getPower()).isEqualTo(2);
        assertThat(token.getCard().getToughness()).isEqualTo(2);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.DRAGON);
        assertThat(token.getCard().getKeywords()).contains(Keyword.FLYING);
        assertThat(token.getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("An opponent's Aura attaching to Brood Keeper still creates a token for its controller")
    void opponentAuraTriggersForKeeperController() {
        Permanent keeper = addCreatureReady(player1, new BroodKeeper());

        enchantWithEternalThirst(player2, keeper);

        assertThat(findPermanents(player1, "Dragon")).hasSize(1);
        assertThat(findPermanents(player2, "Dragon")).isEmpty();
    }

    @Test
    @DisplayName("An Aura attaching to another creature does not trigger Brood Keeper")
    void auraOnOtherCreatureDoesNotTrigger() {
        addCreatureReady(player1, new BroodKeeper());
        Permanent bears = addCreatureReady(player1, new RuneclawBear());

        enchantWithEternalThirst(player1, bears);

        assertThat(findPermanents(player1, "Dragon")).isEmpty();
    }

    @Test
    @DisplayName("The Dragon token's {R} ability gives it +1/+0 until end of turn")
    void dragonTokenHasFirebreathing() {
        Permanent keeper = addCreatureReady(player1, new BroodKeeper());

        enchantWithEternalThirst(player1, keeper);

        int tokenIndex = gd.playerBattlefields.get(player1.getId())
                .indexOf(findPermanents(player1, "Dragon").getFirst());

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, tokenIndex, null, null);
        harness.passBothPriorities();

        Permanent token = findPermanents(player1, "Dragon").getFirst();
        assertThat(token.getEffectivePower()).isEqualTo(3);
        assertThat(token.getEffectiveToughness()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(token.getEffectivePower()).isEqualTo(2);
    }

    @Test
    @DisplayName("Firebreathing can be activated repeatedly without tapping a newly created Dragon")
    void firebreathingActivationsStack() {
        Permanent keeper = addCreatureReady(player1, new BroodKeeper());
        enchantWithEternalThirst(player1, keeper);
        Permanent token = findPermanent(player1, "Dragon");
        int tokenIndex = gd.playerBattlefields.get(player1.getId()).indexOf(token);

        harness.addMana(player1, ManaColor.RED, 2);
        harness.activateAbility(player1, tokenIndex, null, null);
        harness.activateAbility(player1, tokenIndex, null, null);
        resolveAllTriggers();

        assertThat(token.getEffectivePower()).isEqualTo(4);
        assertThat(token.getEffectiveToughness()).isEqualTo(2);
        assertThat(token.isTapped()).isFalse();
        assertThat(keeper.getEffectivePower()).isEqualTo(2);
    }

    @Test
    @DisplayName("Each newly attached Aura creates another Dragon")
    void multipleAurasEachCreateToken() {
        Permanent keeper = addCreatureReady(player1, new BroodKeeper());

        enchantWithEternalThirst(player1, keeper);
        enchantWithEternalThirst(player1, keeper);

        assertThat(findPermanents(player1, "Dragon")).hasSize(2);
    }

    @Test
    @DisplayName("Only the Keeper receiving the Aura triggers")
    void multipleKeepersDoNotMultiplyTrigger() {
        Permanent keeper = addCreatureReady(player1, new BroodKeeper());
        addCreatureReady(player1, new BroodKeeper());

        enchantWithEternalThirst(player1, keeper);

        assertThat(findPermanents(player1, "Dragon")).hasSize(1);
    }

    @Test
    @DisplayName("The attachment trigger still creates a Dragon after Brood Keeper dies")
    void triggerResolvesAfterKeeperDies() {
        Permanent keeper = addCreatureReady(player1, new BroodKeeper());
        harness.setHand(player1, List.of(new EternalThirst()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castEnchantment(player1, 0, keeper.getId());
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Dragon")).isEmpty();

        harness.setHand(player2, List.of(new FleshToDust()));
        harness.addMana(player2, ManaColor.BLACK, 5);
        harness.castAndResolveInstant(player2, 0, keeper.getId());
        assertThat(findPermanents(player1, "Brood Keeper")).isEmpty();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Dragon")).hasSize(1);
    }

    @Test
    @DisplayName("Moving an existing Aura onto Brood Keeper creates a Dragon")
    void movingAuraOntoKeeperTriggers() {
        Permanent keeper = addCreatureReady(player1, new BroodKeeper());
        Permanent bear = addCreatureReady(player1, new RuneclawBear());
        harness.setHand(player1, List.of(new DetainmentSpell()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castEnchantment(player1, 0, bear.getId());
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Dragon")).isEmpty();

        Permanent aura = findPermanent(player1, "Detainment Spell");
        int auraIndex = gd.playerBattlefields.get(player1.getId()).indexOf(aura);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, auraIndex, null, keeper.getId());
        resolveAllTriggers();

        assertThat(aura.getAttachedTo()).isEqualTo(keeper.getId());
        assertThat(findPermanents(player1, "Dragon")).hasSize(1);
    }

    @Test
    @DisplayName("Attaching an Aura to the Keeper it already enchants does not create another Dragon")
    void attachingAuraToSameKeeperDoesNotTrigger() {
        Permanent keeper = addCreatureReady(player1, new BroodKeeper());
        harness.setHand(player1, List.of(new DetainmentSpell()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castEnchantment(player1, 0, keeper.getId());
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Dragon")).hasSize(1);

        Permanent aura = findPermanent(player1, "Detainment Spell");
        int auraIndex = gd.playerBattlefields.get(player1.getId()).indexOf(aura);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, auraIndex, null, keeper.getId());
        resolveAllTriggers();

        assertThat(aura.getAttachedTo()).isEqualTo(keeper.getId());
        assertThat(findPermanents(player1, "Dragon")).hasSize(1);
    }

    private void enchantWithEternalThirst(Player controller, Permanent target) {
        harness.forceActivePlayer(controller);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(controller, List.of(new EternalThirst()));
        harness.addMana(controller, ManaColor.BLACK, 2);

        harness.castEnchantment(controller, 0, target.getId());
        resolveAllTriggers();
    }
}
