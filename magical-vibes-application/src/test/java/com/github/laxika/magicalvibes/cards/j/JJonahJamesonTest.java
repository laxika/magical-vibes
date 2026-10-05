package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.a.AirtightAlibi;
import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.f.FathomFleetCaptain;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JJonahJameson.class, DoomBlade.class, FathomFleetCaptain.class, GrizzlyBears.class, AirtightAlibi.class})
class JJonahJamesonTest extends BaseCardTest {

    @Test
    @DisplayName("ETB suspects up to one target creature")
    void suspectsTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castJonah(target);

        assertThat(gqs.hasKeyword(gd, target, Keyword.MENACE)).isTrue();
        assertThat(bls.canBlock(gd, target)).isFalse();
    }

    @Test
    @DisplayName("ETB may choose no creature to suspect")
    void mayChooseNoTarget() {
        harness.setHand(player1, List.of(new JJonahJameson()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "J. Jonah Jameson")).isNotNull();
    }

    @Test
    @DisplayName("The suspected designation remains after J. Jonah Jameson leaves")
    void suspicionRemainsAfterSourceLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent jonah = castJonah(target);

        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, jonah.getId());

        assertThat(gqs.hasKeyword(gd, target, Keyword.MENACE)).isTrue();
        assertThat(bls.canBlock(gd, target)).isFalse();
    }

    @Test
    @DisplayName("Creates a Treasure when a menace creature you control attacks")
    void createsTreasureForMenaceAttacker() {
        addCreatureReady(player1, new JJonahJameson());
        addCreatureReady(player1, new FathomFleetCaptain());

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("Does not create a Treasure for an attacking creature without menace")
    void doesNotCreateTreasureForNonMenaceAttacker() {
        addCreatureReady(player1, new JJonahJameson());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Each menace attacker creates its own Treasure")
    void createsTreasureForEachMenaceAttacker() {
        addCreatureReady(player1, new FathomFleetCaptain());
        Permanent suspected = addCreatureReady(player1, new GrizzlyBears());
        castJonah(suspected);
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1, 3));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
    }

    @Test
    @DisplayName("An opponent's menace attacker does not create a Treasure")
    void doesNotTriggerForOpposingAttacker() {
        addCreatureReady(player1, new JJonahJameson());
        addCreatureReady(player2, new FathomFleetCaptain());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Airtight Alibi removes suspicion applied by Jameson")
    void alibiRemovesSuspicion() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castJonah(target);
        castAlibi(target);

        assertThat(gqs.hasKeyword(gd, target, Keyword.MENACE)).isFalse();
        assertThat(bls.canBlock(gd, target)).isTrue();
    }

    @Test
    @DisplayName("A creature enchanted with Airtight Alibi cannot become suspected")
    void respectsCannotBecomeSuspected() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castAlibi(target);
        castJonah(target);

        assertThat(gqs.hasKeyword(gd, target, Keyword.MENACE)).isFalse();
        assertThat(bls.canBlock(gd, target)).isTrue();
    }

    private void castAlibi(Permanent target) {
        harness.setHand(player1, List.of(new AirtightAlibi()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
        resolveAllTriggers();
    }

    private Permanent castJonah(Permanent target) {
        harness.setHand(player1, List.of(new JJonahJameson()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0, List.of(target.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "J. Jonah Jameson");
    }
}
