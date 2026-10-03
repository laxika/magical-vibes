package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BloodstoneGoblin;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CoruscationMage.class, GrizzlyBears.class, Spellbook.class, Shock.class, BloodstoneGoblin.class})
class CoruscationMageTest extends BaseCardTest {

    @Test
    void offspringCreatesOneOneTokenCopyWhenPaid() {
        harness.setHand(player1, List.of(new CoruscationMage()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castKickedCreature(player1, 0);
        resolveAllTriggers();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(1);
        assertThat(tokens.getFirst().getEffectivePower()).isEqualTo(1);
        assertThat(tokens.getFirst().getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    void doesNotCreateOffspringTokenWhenNotPaid() {
        harness.setHand(player1, List.of(new CoruscationMage()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void noncreatureSpellDealsOneDamageToEachOpponent() {
        harness.addToBattlefield(player1, new CoruscationMage());
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    void creatureSpellDoesNotTriggerDamage() {
        harness.addToBattlefield(player1, new CoruscationMage());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    void offspringCopyAlsoDealsDamageForNoncreatureSpells() {
        harness.setHand(player1, List.of(new CoruscationMage(), new Spellbook()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castKickedCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Coruscation Mage")).hasSize(2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(findPermanents(player1, "Coruscation Mage")).hasSize(2);
    }

    @Test
    void opponentsNoncreatureSpellDoesNotTriggerDamage() {
        harness.addToBattlefield(player1, new CoruscationMage());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    void offspringStillCreatesCopyAfterSourceDies() {
        harness.setHand(player1, List.of(new CoruscationMage()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        Permanent mage = findPermanent(player1, "Coruscation Mage");

        harness.castAndResolveInstant(player2, 0, mage.getId());
        assertThat(findPermanents(player1, "Coruscation Mage")).isEmpty();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Coruscation Mage")).singleElement()
                .satisfies(token -> {
                    assertThat(token.getCard().isToken()).isTrue();
                    assertThat(token.getEffectivePower()).isEqualTo(1);
                    assertThat(token.getEffectiveToughness()).isEqualTo(1);
                });
    }

    @Test
    void payingOffspringDoesNotTriggerKickedSpellAbilities() {
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new BloodstoneGoblin());
        harness.setHand(player1, List.of(new CoruscationMage()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castKickedCreature(player1, 0);
        resolveAllTriggers();

        assertThat(goblin.getPowerModifier()).isZero();
        assertThat(goblin.getToughnessModifier()).isZero();
        assertThat(findPermanents(player1, "Coruscation Mage")).hasSize(2);
    }
}
