package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.l.LotusPetal;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
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

@CardUsed({KoglaTheTitanApe.class, GrizzlyBears.class, AngelicChorus.class, EliteVanguard.class,
        LlanowarElves.class, LotusPetal.class, Unsummon.class})
class KoglaTheTitanApeTest extends BaseCardTest {

    @Test
    @DisplayName("ETB can make Kogla fight up to one opposing creature")
    void etbFightsChosenCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castKogla(List.of(target.getId()));

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("ETB can choose no creature")
    void etbCanChooseNoCreature() {
        castKogla(List.of());

        harness.assertOnBattlefield(player1, "Kogla, the Titan Ape");
    }

    @Test
    @DisplayName("Attacking destroys an artifact or enchantment defending player controls")
    void attackDestroysDefendingArtifactOrEnchantment() {
        Permanent kogla = harness.addToBattlefieldAndReturn(player1, new KoglaTheTitanApe());
        kogla.setSummoningSick(false);
        kogla.setAttackTarget(player2.getId());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new LotusPetal());

        declareAttackAndResolveTrigger(kogla);

        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Lotus Petal");
    }

    @Test
    @DisplayName("Attacking can destroy a defending enchantment")
    void attackDestroysDefendingEnchantment() {
        Permanent kogla = harness.addToBattlefieldAndReturn(player1, new KoglaTheTitanApe());
        kogla.setSummoningSick(false);
        kogla.setAttackTarget(player2.getId());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new AngelicChorus());

        declareAttackAndResolveTrigger(kogla);

        harness.handlePermanentChosen(player1, enchantment.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Angelic Chorus");
    }

    @Test
    @DisplayName("Attack trigger cannot target a permanent the attacker controls")
    void attackTriggerRequiresDefendingPlayerPermanent() {
        Permanent kogla = harness.addToBattlefieldAndReturn(player1, new KoglaTheTitanApe());
        kogla.setSummoningSick(false);
        kogla.setAttackTarget(player2.getId());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new LotusPetal());
        Permanent legalTarget = harness.addToBattlefieldAndReturn(player2, new LotusPetal());

        declareAttackAndResolveTrigger(kogla);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, legalTarget.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Lotus Petal");
        harness.assertInGraveyard(player2, "Lotus Petal");
    }

    @Test
    @DisplayName("The activated ability returns a Human and gives Kogla indestructible")
    void activatedAbilityReturnsHumanAndGrantsIndestructible() {
        Permanent kogla = harness.addToBattlefieldAndReturn(player1, new KoglaTheTitanApe());
        Permanent human = harness.addToBattlefieldAndReturn(player1, new EliteVanguard());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, human.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(human);
        assertThat(gd.playerHands.get(player1.getId())).contains(human.getCard());
        assertThat(gqs.hasKeyword(gd, kogla, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("The activated ability only targets a Human the controller controls")
    void activatedAbilityRequiresControlledHuman() {
        harness.addToBattlefield(player1, new KoglaTheTitanApe());
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, elf.getId()))
                .isInstanceOf(IllegalStateException.class);

        Permanent opposingHuman = harness.addToBattlefieldAndReturn(player2, new EliteVanguard());
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opposingHuman.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fight deals the opposing creature's power as damage to Kogla")
    void fightDealsDamageToKoglaToo() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castKogla(List.of(target.getId()));

        Permanent kogla = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(kogla.getMarkedDamage()).isEqualTo(2);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Kogla does not fight if it leaves before the enter trigger resolves")
    void fightDoesNothingWhenKoglaLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new KoglaTheTitanApe()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0, List.of(target.getId()));
        harness.passBothPriorities();
        Permanent kogla = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player2, 0, kogla.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(target.getMarkedDamage()).isZero();
        harness.assertNotOnBattlefield(player1, "Kogla, the Titan Ape");
    }

    @Test
    @DisplayName("An illegal Human target prevents Kogla from gaining indestructible")
    void illegalHumanTargetPreventsIndestructible() {
        Permanent kogla = harness.addToBattlefieldAndReturn(player1, new KoglaTheTitanApe());
        Permanent human = harness.addToBattlefieldAndReturn(player1, new EliteVanguard());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, human.getId());
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player2, 0, human.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, kogla, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).contains(human.getCard());
    }

    @Test
    @DisplayName("The Human still returns if Kogla leaves before its ability resolves")
    void humanReturnsWhenKoglaLeaves() {
        Permanent kogla = harness.addToBattlefieldAndReturn(player1, new KoglaTheTitanApe());
        Permanent human = harness.addToBattlefieldAndReturn(player1, new EliteVanguard());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, human.getId());
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player2, 0, kogla.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(kogla.getCard(), human.getCard());
        harness.assertNotOnBattlefield(player1, "Elite Vanguard");
    }

    @Test
    @DisplayName("Indestructible expires at the end of the turn")
    void indestructibleExpires() {
        Permanent kogla = harness.addToBattlefieldAndReturn(player1, new KoglaTheTitanApe());
        Permanent human = harness.addToBattlefieldAndReturn(player1, new EliteVanguard());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, human.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, kogla, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, kogla, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Kogla can decline to fight even when an opposing creature is available")
    void canDeclineAvailableFight() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castKogla(List.of());

        harness.assertOnBattlefield(player1, "Kogla, the Titan Ape");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("An illegal fight target deals no damage to Kogla")
    void fightDoesNothingWhenTargetLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new KoglaTheTitanApe()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0, List.of(target.getId()));
        harness.passBothPriorities();
        Permanent kogla = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Kogla, the Titan Ape");
        assertThat(kogla.getMarkedDamage()).isZero();
        assertThat(gd.playerHands.get(player2.getId())).contains(target.getCard());
    }

    @Test
    @DisplayName("The attack trigger cannot target an ordinary defending creature")
    void attackTriggerRejectsNonArtifactNonEnchantment() {
        Permanent kogla = harness.addToBattlefieldAndReturn(player1, new KoglaTheTitanApe());
        kogla.setSummoningSick(false);
        kogla.setAttackTarget(player2.getId());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new LotusPetal());

        declareAttackAndResolveTrigger(kogla);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Lotus Petal");
    }

    private void castKogla(List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new KoglaTheTitanApe()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0, targetIds);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void declareAttackAndResolveTrigger(Permanent kogla) {
        int koglaIndex = gd.playerBattlefields.get(player1.getId()).indexOf(kogla);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(koglaIndex));
        harness.passBothPriorities();
    }
}
