package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.Dominate;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SilkWebWeaver.class, GrizzlyBears.class, Dominate.class})
class SilkWebWeaverTest extends BaseCardTest {

    @Test
    @DisplayName("Can be cast with web-slinging by returning a tapped creature")
    void castsWithWebSlinging() {
        Permanent tappedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        tappedCreature.tap();
        harness.setHand(player1, List.of(new SilkWebWeaver()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreatureWithAlternateCost(player1, 0, List.of(tappedCreature.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Silk, Web Weaver");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Casting a creature spell creates a Human Citizen token")
    void creatureSpellCreatesToken() {
        harness.addToBattlefield(player1, new SilkWebWeaver());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent token = findPermanent(player1, "Human Citizen");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
    }

    @Test
    @DisplayName("Activated ability boosts own creatures and grants vigilance until end of turn")
    void activatedAbilityBoostsOwnCreatures() {
        Permanent silk = harness.addToBattlefieldAndReturn(player1, new SilkWebWeaver());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, silk)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, silk)).isEqualTo(7);
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, silk, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.VIGILANCE)).isFalse();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Web-slinging cannot return an untapped creature")
    void webSlingingRejectsUntappedCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SilkWebWeaver()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreatureWithAlternateCost(
                player1, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Silk, Web Weaver");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Silk does not trigger for its own casting")
    void normalCastingDoesNotCreateToken() {
        harness.setHand(player1, List.of(new SilkWebWeaver()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Silk, Web Weaver");
        assertThat(countPermanents(player1, "Human Citizen")).isZero();
    }

    @Test
    @DisplayName("Returning Silk for web-slinging removes its cast trigger before casting")
    void returningSilkForWebSlingingDoesNotCreateToken() {
        Permanent silk = harness.addToBattlefieldAndReturn(player1, new SilkWebWeaver());
        silk.tap();
        harness.setHand(player1, List.of(new SilkWebWeaver()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreatureWithAlternateCost(player1, 0, List.of(silk.getId()));
        resolveAllTriggers();

        harness.assertInHand(player1, "Silk, Web Weaver");
        assertThat(countPermanents(player1, "Silk, Web Weaver")).isEqualTo(1);
        assertThat(countPermanents(player1, "Human Citizen")).isZero();
    }

    @Test
    @DisplayName("Opponent creature spells do not create tokens")
    void opponentCreatureSpellDoesNotCreateToken() {
        harness.addToBattlefield(player1, new SilkWebWeaver());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player2, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(countPermanents(player1, "Human Citizen")).isZero();
        assertThat(countPermanents(player2, "Human Citizen")).isZero();
    }

    @Test
    @DisplayName("A cast trigger resolves before the creature spell")
    void tokenEntersBeforeCreatureSpellResolves() {
        harness.addToBattlefield(player1, new SilkWebWeaver());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Human Citizen")).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Creatures entering after the activation resolves are not boosted")
    void activationDoesNotAffectLaterCreatures() {
        harness.addToBattlefield(player1, new SilkWebWeaver());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, 0, null, null);
        resolveAllTriggers();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent creature = findPermanent(player1, "Grizzly Bears");
        Permanent token = findPermanent(player1, "Human Citizen");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, token, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Casting a noncreature spell does not create a token")
    void noncreatureSpellDoesNotCreateToken() {
        harness.addToBattlefield(player1, new SilkWebWeaver());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Dominate()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0, 2, target.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(countPermanents(player1, "Human Citizen")).isZero();
    }

    @Test
    @DisplayName("Silk controlled by an opponent at resolution does not gain vigilance")
    void stolenSilkDoesNotGainVigilanceFromPreviousControllersActivation() {
        Permanent silk = harness.addToBattlefieldAndReturn(player1, new SilkWebWeaver());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, 0, null, null);

        harness.setHand(player2, List.of(new Dominate()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 5);
        harness.castInstant(player2, 0, 4, silk.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Silk, Web Weaver");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, silk)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, silk)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, silk, Keyword.VIGILANCE)).isFalse();
        assertThat(countPermanents(player1, "Human Citizen")).isZero();
    }
}
