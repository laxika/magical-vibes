package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.ArtificialEvolution;
import com.github.laxika.magicalvibes.cards.b.Bitterblossom;
import com.github.laxika.magicalvibes.cards.c.CopperMyr;
import com.github.laxika.magicalvibes.cards.i.IronMyr;
import com.github.laxika.magicalvibes.cards.s.SylvokReplica;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MyrGalvanizer.class, CopperMyr.class, IronMyr.class, SylvokReplica.class})
class MyrGalvanizerTest extends BaseCardTest {

    @Test
    @DisplayName("Other Myr creatures you control get +1/+1")
    void buffsOtherOwnMyr() {
        harness.addToBattlefield(player1, new MyrGalvanizer());
        harness.addToBattlefield(player1, new CopperMyr());

        Permanent copperMyr = findPermanent(player1, "Copper Myr");

        // Copper Myr is 1/1 base, should be 2/2 with Galvanizer
        assertThat(gqs.getEffectivePower(gd, copperMyr)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, copperMyr)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not buff itself")
    void doesNotBuffItself() {
        harness.addToBattlefield(player1, new MyrGalvanizer());

        Permanent galvanizer = findPermanent(player1, "Myr Galvanizer");

        // 2/2 base, no self-buff
        assertThat(gqs.getEffectivePower(gd, galvanizer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, galvanizer)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not buff non-Myr creatures")
    void doesNotBuffNonMyr() {
        harness.addToBattlefield(player1, new MyrGalvanizer());
        harness.addToBattlefield(player1, new SylvokReplica());

        Permanent replica = findPermanent(player1, "Sylvok Replica");

        assertThat(gqs.getEffectivePower(gd, replica)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, replica)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not buff opponent's Myr creatures")
    void doesNotBuffOpponentMyr() {
        harness.addToBattlefield(player1, new MyrGalvanizer());
        harness.addToBattlefield(player2, new CopperMyr());

        Permanent opponentMyr = findPermanent(player2, "Copper Myr");

        // Should remain 1/1 — only buffs YOUR Myr
        assertThat(gqs.getEffectivePower(gd, opponentMyr)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentMyr)).isEqualTo(1);
    }

    @Test
    @DisplayName("Two Galvanizers buff each other")
    void twoGalvanizersBuffEachOther() {
        harness.addToBattlefield(player1, new MyrGalvanizer());
        harness.addToBattlefield(player1, new MyrGalvanizer());

        List<Permanent> galvanizers = findPermanents(player1, "Myr Galvanizer");

        assertThat(galvanizers).hasSize(2);
        for (Permanent g : galvanizers) {
            // 2/2 base + 1/1 from the other Galvanizer = 3/3
            assertThat(gqs.getEffectivePower(gd, g)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, g)).isEqualTo(3);
        }
    }

    @Test
    @DisplayName("Bonus is removed when Myr Galvanizer leaves the battlefield")
    void bonusRemovedWhenGalvanizerLeaves() {
        harness.addToBattlefield(player1, new MyrGalvanizer());
        harness.addToBattlefield(player1, new CopperMyr());

        Permanent copperMyr = findPermanent(player1, "Copper Myr");

        assertThat(gqs.getEffectivePower(gd, copperMyr)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Myr Galvanizer"));

        assertThat(gqs.getEffectivePower(gd, copperMyr)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, copperMyr)).isEqualTo(1);
    }

    @Test
    @DisplayName("Activating ability untaps other tapped Myr you control")
    void untapsOtherTappedMyr() {
        Permanent galvanizer = addCreatureReady(player1, new MyrGalvanizer());

        Permanent copperMyr = addCreatureReady(player1, new CopperMyr());
        copperMyr.tap();

        Permanent ironMyr = addCreatureReady(player1, new IronMyr());
        ironMyr.tap();

        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int galvanizerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(galvanizer);
        harness.activateAbility(player1, galvanizerIndex, null, null);
        harness.passBothPriorities();

        assertThat(copperMyr.isTapped()).isFalse();
        assertThat(ironMyr.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Activating ability does not untap Galvanizer itself")
    void doesNotUntapSelf() {
        Permanent galvanizer = addCreatureReady(player1, new MyrGalvanizer());

        Permanent copperMyr = addCreatureReady(player1, new CopperMyr());
        copperMyr.tap();

        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int galvanizerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(galvanizer);
        harness.activateAbility(player1, galvanizerIndex, null, null);
        harness.passBothPriorities();

        // Galvanizer tapped as cost — should remain tapped
        assertThat(galvanizer.isTapped()).isTrue();
        assertThat(copperMyr.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Activating ability does not untap non-Myr creatures")
    void doesNotUntapNonMyr() {
        Permanent galvanizer = addCreatureReady(player1, new MyrGalvanizer());

        Permanent replica = addCreatureReady(player1, new SylvokReplica());
        replica.tap();

        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int galvanizerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(galvanizer);
        harness.activateAbility(player1, galvanizerIndex, null, null);
        harness.passBothPriorities();

        // Sylvok Replica is not a Myr — should stay tapped
        assertThat(replica.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activating ability does not untap opponent's Myr")
    void doesNotUntapOpponentMyr() {
        Permanent galvanizer = addCreatureReady(player1, new MyrGalvanizer());

        Permanent opponentMyr = addCreatureReady(player2, new CopperMyr());
        opponentMyr.tap();

        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int galvanizerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(galvanizer);
        harness.activateAbility(player1, galvanizerIndex, null, null);
        harness.passBothPriorities();

        assertThat(opponentMyr.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activating ability puts entry on the stack")
    void abilityPutsEntryOnStack() {
        Permanent galvanizer = addCreatureReady(player1, new MyrGalvanizer());

        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int galvanizerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(galvanizer);
        harness.activateAbility(player1, galvanizerIndex, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Myr Galvanizer");
    }

    @Test
    @DisplayName("Untaps another Galvanizer, but leaves the activating Galvanizer tapped")
    void untapsAnotherGalvanizer() {
        Permanent source = addCreatureReady(player1, new MyrGalvanizer());
        Permanent other = addCreatureReady(player1, new MyrGalvanizer());
        other.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(source.isTapped()).isTrue();
        assertThat(other.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Untaps a Myr that entered after activation, even with summoning sickness")
    void checksMyrAtResolution() {
        addCreatureReady(player1, new MyrGalvanizer());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);

        Permanent lateMyr = harness.addToBattlefieldAndReturn(player1, new CopperMyr());
        lateMyr.setSummoningSick(true);
        lateMyr.tap();
        harness.passBothPriorities();

        assertThat(lateMyr.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The activated ability resolves after its source leaves the battlefield")
    void resolvesWithoutSource() {
        Permanent source = addCreatureReady(player1, new MyrGalvanizer());
        Permanent myr = addCreatureReady(player1, new CopperMyr());
        myr.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);

        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        harness.passBothPriorities();

        assertThat(myr.isTapped()).isFalse();
    }

    @Test
    @CardUsed({Bitterblossom.class, ArtificialEvolution.class})
    @DisplayName("Untaps a noncreature kindred permanent with the Myr subtype")
    void untapsNoncreatureMyrPermanent() {
        Permanent source = addCreatureReady(player1, new MyrGalvanizer());
        Permanent blossom = harness.addToBattlefieldAndReturn(player1, new Bitterblossom());
        harness.setHand(player1, List.of(new ArtificialEvolution()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, blossom.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "FAERIE");
        harness.handleListChoice(player1, "MYR");

        assertThat(gqs.hasEffectiveSubtype(gd, blossom, CardSubtype.MYR)).isTrue();
        assertThat(gqs.isCreature(gd, blossom)).isFalse();
        blossom.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(source.isTapped()).isTrue();
        assertThat(blossom.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A summoning-sick Galvanizer cannot pay its tap cost")
    void cannotActivateWhileSummoningSick() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new MyrGalvanizer());
        source.setSummoningSick(true);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(source.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Galvanizer cannot pay its tap cost")
    void cannotActivateWhileTapped() {
        Permanent source = addCreatureReady(player1, new MyrGalvanizer());
        source.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }
}
