package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Firebolt;
import com.github.laxika.magicalvibes.cards.h.HowlingGale;
import com.github.laxika.magicalvibes.cards.p.PardicFirecat;
import com.github.laxika.magicalvibes.cards.s.ScorchingMissile;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SphereOfLaw.class, Firebolt.class, HowlingGale.class, ScorchingMissile.class, PardicFirecat.class})
class SphereOfLawTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents 2 damage from a red source")
    void preventsDamageFromRedSource() {
        harness.addToBattlefield(player1, new SphereOfLaw());
        harness.setLife(player1, 20);
        harness.setHand(player2, List.of(new Firebolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.forceActivePlayer(player2);
        harness.castAndResolveSorcery(player2, 0, player1.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Does not prevent damage from a non-red source")
    void doesNotPreventDamageFromNonRedSource() {
        harness.addToBattlefield(player1, new SphereOfLaw());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new HowlingGale()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player2, 0);

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Protects only its controller from red damage")
    void protectsOnlyItsController() {
        harness.addToBattlefield(player1, new SphereOfLaw());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new Firebolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.forceActivePlayer(player2);
        harness.castAndResolveSorcery(player2, 0, player2.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Prevents only 2 damage from a red source")
    void preventsOnlyTwoDamageFromRedSource() {
        harness.addToBattlefield(player1, new SphereOfLaw());
        harness.setLife(player1, 20);
        harness.setHand(player2, List.of(new ScorchingMissile()));
        harness.addMana(player2, ManaColor.RED, 4);

        harness.forceActivePlayer(player2);
        harness.castAndResolveSorcery(player2, 0, player1.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Prevents 2 damage from each red combat source")
    void preventsDamageFromEachRedCombatSource() {
        harness.addToBattlefield(player1, new SphereOfLaw());
        harness.setLife(player1, 20);

        addCreatureReady(player2, new PardicFirecat());
        addCreatureReady(player2, new PardicFirecat());

        declareAttackers(player2, List.of(0, 1));
        resolveCombat(player2);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Multiple copies each prevent 2 damage")
    void multipleCopiesStack() {
        harness.addToBattlefield(player1, new SphereOfLaw());
        harness.addToBattlefield(player1, new SphereOfLaw());
        harness.setLife(player1, 20);
        harness.setHand(player2, List.of(new ScorchingMissile()));
        harness.addMana(player2, ManaColor.RED, 4);
        harness.forceActivePlayer(player2);

        harness.castAndResolveSorcery(player2, 0, player1.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Prevention applies again when the same card deals damage through flashback")
    void preventsEachDamageEventFromSameCard() {
        harness.addToBattlefield(player1, new SphereOfLaw());
        harness.setLife(player1, 20);
        harness.setHand(player2, List.of(new ScorchingMissile()));
        harness.addMana(player2, ManaColor.RED, 14);
        harness.forceActivePlayer(player2);

        harness.castAndResolveSorcery(player2, 0, player1.getId());
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);

        harness.castAndResolveFlashback(player2, 0, player1.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Does not protect its controller's creatures")
    void doesNotPreventDamageToCreatures() {
        harness.addToBattlefield(player1, new SphereOfLaw());
        var creature = harness.addToBattlefieldAndReturn(player1, new PardicFirecat());
        harness.setHand(player2, List.of(new Firebolt(), new Firebolt()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.forceActivePlayer(player2);

        harness.castAndResolveSorcery(player2, 0, creature.getId());
        harness.assertOnBattlefield(player1, "Pardic Firecat");
        harness.castAndResolveSorcery(player2, 0, creature.getId());

        harness.assertNotOnBattlefield(player1, "Pardic Firecat");
    }

    @Test
    @DisplayName("Prevents red damage from its controller's own spell")
    void preventsDamageFromOwnSource() {
        harness.addToBattlefield(player1, new SphereOfLaw());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new ScorchingMissile()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }
}
