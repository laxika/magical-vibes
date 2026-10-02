package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.o.OtterPenguin;
import com.github.laxika.magicalvibes.cards.l.LightningStrike;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({
        AangSwiftSavior.class,
        AangAndLaOceansFury.class,
        OtterPenguin.class,
        LightningStrike.class
})
class AangSwiftSaviorTest extends BaseCardTest {

    @Test
    @DisplayName("ETB airbends another creature and grants its owner a generic alternative cost")
    void airbendsAnotherCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new OtterPenguin());
        harness.setHand(player1, List.of(new AangSwiftSavior()));
        addAangMana();

        harness.castCreature(player1, 0, 0);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(target.getId());

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(target.getOriginalCard().getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(target.getOriginalCard().getId())).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("ETB can airbend a spell on the stack")
    void airbendsSpellOnStack() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new OtterPenguin());
        LightningStrike lightningStrike = new LightningStrike();
        AangSwiftSavior aangCard = new AangSwiftSavior();
        harness.setHand(player1, List.of(lightningStrike, aangCard));
        harness.addMana(player1, ManaColor.RED, 1);
        addAangMana();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, creature.getId());
        harness.castCreature(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getId().equals(lightningStrike.getId()));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(lightningStrike.getId());

        harness.handlePermanentChosen(player1, lightningStrike.getId());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(lightningStrike.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(lightningStrike.getId())).isEqualTo(player1.getId());
        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getId().equals(lightningStrike.getId())
                && entry.getEntryType() == StackEntryType.INSTANT_SPELL);
    }

    @Test
    @DisplayName("Waterbend transforms Aang")
    void waterbendTransformsAang() {
        Permanent aang = harness.addToBattlefieldAndReturn(player1, new AangSwiftSavior());
        for (int i = 0; i < 7; i++) {
            addCreatureReady(player1, new OtterPenguin());
        }

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(aang.isTransformed()).isTrue();
        assertThat(aang.getCard()).isInstanceOf(AangAndLaOceansFury.class);
    }

    @Test
    @DisplayName("Aang and La puts counters on each tapped creature you control when it attacks")
    void attackCountersTappedCreatures() {
        Permanent aang = harness.addToBattlefieldAndReturn(player1, new AangSwiftSavior());
        aang.setCard(aang.getOriginalCard().getBackFaceCard());
        aang.setTransformed(true);
        aang.setSummoningSick(false);
        Permanent tappedCreature = addCreatureReady(player1, new OtterPenguin());
        Permanent untappedCreature = addCreatureReady(player1, new OtterPenguin());
        tappedCreature.tap();

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(aang.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(tappedCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(untappedCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Two pending waterbend activations leave Aang on his back face")
    void pendingWaterbendActivationsDoNotTransformBack() {
        Permanent aang = harness.addToBattlefieldAndReturn(player1, new AangSwiftSavior());
        harness.addMana(player1, ManaColor.COLORLESS, 16);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(aang.isTransformed()).isTrue();
        assertThat(aang.getCard()).isInstanceOf(AangAndLaOceansFury.class);
    }

    @Test
    @DisplayName("Aang can enter without airbending when there are no other creatures or spells")
    void entersWithoutLegalAirbendTarget() {
        harness.setHand(player1, List.of(new AangSwiftSavior()));
        addAangMana();

        harness.castCreature(player1, 0, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Aang, Swift Savior");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    @DisplayName("An airbent spell can be recast for two generic mana")
    void recastsAirbentSpellForGenericMana() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new OtterPenguin());
        LightningStrike strike = new LightningStrike();
        harness.setHand(player1, List.of(strike, new AangSwiftSavior()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        addAangMana();
        harness.castInstant(player1, 0, target.getId());
        harness.castCreature(player1, 0, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, strike.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromExile(player1, strike.getId(), target.getId());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(strike.getId())).isNull();
        harness.assertInGraveyard(player1, "Lightning Strike");
        harness.assertInGraveyard(player2, "Otter-Penguin");
    }

    @Test
    @DisplayName("The attack trigger checks tapped creatures and their controller at resolution")
    void attackCountersUseTappedStateAtResolution() {
        Permanent aang = addCreatureReady(player1, new AangSwiftSavior());
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        Permanent untappedBeforeResolution = addCreatureReady(player1, new OtterPenguin());
        Permanent tappedBeforeResolution = addCreatureReady(player1, new OtterPenguin());
        Permanent opponent = addCreatureReady(player2, new OtterPenguin());
        untappedBeforeResolution.tap();
        opponent.tap();

        declareAttackers(player1, List.of(0));
        untappedBeforeResolution.untap();
        tappedBeforeResolution.tap();
        resolveAllTriggers();

        assertThat(aang.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(untappedBeforeResolution.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(tappedBeforeResolution.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Aang cannot target himself and may decline to airbend another creature")
    void mayDeclineAirbendWithLegalTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new OtterPenguin());
        harness.setHand(player1, List.of(new AangSwiftSavior()));
        addAangMana();
        harness.castCreature(player1, 0, 0);
        harness.passBothPriorities();

        Permanent aang = findPermanent(player1, "Aang, Swift Savior");
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(target.getId()).doesNotContain(aang.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Aang, Swift Savior");
        harness.assertOnBattlefield(player2, "Otter-Penguin");
        assertThat(gd.exiledCards).isEmpty();
    }

    private void addAangMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

}
