package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BoggartBrute;
import com.github.laxika.magicalvibes.cards.f.FaerieMiscreant;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SoulWarden;
import com.github.laxika.magicalvibes.cards.s.StoneworkPackbeast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AlliedAssault.class, BoggartBrute.class, FaerieMiscreant.class, FountainOfYouth.class,
        FugitiveWizard.class, GrizzlyBears.class, SoulWarden.class, StoneworkPackbeast.class})
class AlliedAssaultTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts up to two creatures by the size of your party")
    void boostsTwoCreaturesByPartySize() {
        addFullParty();
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castAlliedAssault(List.of(firstTarget.getId(), secondTarget.getId()));

        assertThat(gqs.getEffectivePower(gd, firstTarget)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, firstTarget)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, secondTarget)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, secondTarget)).isEqualTo(6);
    }

    @Test
    @DisplayName("A creature with two party types fills only one role")
    void oneCreatureCannotFillTwoPartyRoles() {
        harness.addToBattlefield(player1,
                partyCreature("Cleric Rogue", CardSubtype.CLERIC, CardSubtype.ROGUE));
        harness.addToBattlefield(player1, new BoggartBrute());
        harness.addToBattlefield(player1, new FugitiveWizard());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castAlliedAssault(List.of(target.getId()));

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(5);
    }

    @Test
    @DisplayName("The boost wears off at cleanup")
    void boostWearsOffAtCleanup() {
        addFullParty();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castAlliedAssault(List.of(target.getId()));
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(6);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent fountain = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new AlliedAssault()));
        addManaForAlliedAssault();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(creature.getId(), fountain.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Can resolve without choosing any targets")
    void canChooseZeroTargets() {
        castAlliedAssault(List.of());

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Allied Assault");
    }

    @Test
    @DisplayName("An opponent's full party does not increase the boost")
    void ignoresOpponentsParty() {
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player2, new StoneworkPackbeast());
        }
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castAlliedAssault(List.of(target.getId()));

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("Multiple creatures of the same party role count only once")
    void duplicateRolesCountOnce() {
        harness.addToBattlefield(player1, new FugitiveWizard());
        harness.addToBattlefield(player1, new FugitiveWizard());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castAlliedAssault(List.of(target.getId()));

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    @DisplayName("One Packbeast fills only one role despite having all four types")
    void packbeastFillsOneRole() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new StoneworkPackbeast());

        castAlliedAssault(List.of(target.getId()));

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("Party size is determined at resolution and the boost then stays fixed")
    void determinesPartySizeAtResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new StoneworkPackbeast());
        harness.setHand(player1, List.of(new AlliedAssault()));
        addManaForAlliedAssault();
        harness.castInstant(player1, 0, List.of(target.getId()));
        harness.addToBattlefield(player1, new StoneworkPackbeast());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);

        harness.addToBattlefield(player1, new StoneworkPackbeast());

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    @DisplayName("Still boosts the remaining target when the other target leaves")
    void resolvesForRemainingTarget() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new StoneworkPackbeast());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new StoneworkPackbeast());
        harness.setHand(player1, List.of(new AlliedAssault()));
        addManaForAlliedAssault();
        harness.castInstant(player1, 0, List.of(first.getId(), second.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(first);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(2);
        harness.assertInGraveyard(player1, "Allied Assault");
    }

    @Test
    @DisplayName("Party size is capped at four even with five flexible party members")
    void partySizeIsCappedAtFour() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new StoneworkPackbeast());
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new StoneworkPackbeast());
        }

        castAlliedAssault(List.of(target.getId()));

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(5);
    }

    @Test
    @DisplayName("Cannot choose more than two targets")
    void cannotChooseThreeTargets() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new StoneworkPackbeast());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new StoneworkPackbeast());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new StoneworkPackbeast());
        harness.setHand(player1, List.of(new AlliedAssault()));
        addManaForAlliedAssault();

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot choose the same creature twice")
    void cannotChooseDuplicateTargets() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new StoneworkPackbeast());
        harness.setHand(player1, List.of(new AlliedAssault()));
        addManaForAlliedAssault();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(target.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addFullParty() {
        harness.addToBattlefield(player1, new SoulWarden());
        harness.addToBattlefield(player1, new FaerieMiscreant());
        harness.addToBattlefield(player1, new BoggartBrute());
        harness.addToBattlefield(player1, new FugitiveWizard());
    }

    private void castAlliedAssault(List<UUID> targetIds) {
        harness.setHand(player1, List.of(new AlliedAssault()));
        addManaForAlliedAssault();
        harness.castAndResolveInstant(player1, 0, targetIds);
    }

    private void addManaForAlliedAssault() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private Card partyCreature(String name, CardSubtype... subtypes) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{2}");
        card.setPower(2);
        card.setToughness(2);
        card.setSubtypes(List.of(subtypes));
        return card;
    }
}
