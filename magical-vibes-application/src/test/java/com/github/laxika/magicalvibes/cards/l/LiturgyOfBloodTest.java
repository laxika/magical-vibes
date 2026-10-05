package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.r.RegathanFirecat;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.i.Indestructibility;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LiturgyOfBlood.class, RegathanFirecat.class, Mountain.class, Indestructibility.class})
class LiturgyOfBloodTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving destroys the target creature and adds {B}{B}{B}")
    void destroysCreatureAndAddsMana() {
        harness.addToBattlefield(player2, new RegathanFirecat());
        harness.setHand(player1, List.of(new LiturgyOfBlood()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        UUID targetId = harness.getPermanentId(player2, "Regathan Firecat");
        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Regathan Firecat");
        harness.assertInGraveyard(player2, "Regathan Firecat");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(3);
        harness.assertInGraveyard(player1, "Liturgy of Blood");
    }

    @Test
    @DisplayName("Fizzles and adds no mana when the target creature is gone")
    void fizzlesWhenTargetRemoved() {
        harness.addToBattlefield(player2, new RegathanFirecat());
        harness.setHand(player1, List.of(new LiturgyOfBlood()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        UUID targetId = harness.getPermanentId(player2, "Regathan Firecat");
        harness.castSorcery(player1, 0, targetId);
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(0);
        harness.assertInGraveyard(player1, "Liturgy of Blood");
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new Mountain());
        harness.setHand(player1, List.of(new LiturgyOfBlood()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        UUID landId = harness.getPermanentId(player2, "Mountain");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, landId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Adds mana even when the legal target is indestructible")
    void addsManaWhenCreatureCannotBeDestroyed() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new RegathanFirecat());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new Indestructibility());
        aura.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new LiturgyOfBlood()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, creature.getId());

        harness.assertOnBattlefield(player2, "Regathan Firecat");
        harness.assertNotInGraveyard(player2, "Regathan Firecat");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLACK)).isZero();
        harness.assertInGraveyard(player1, "Liturgy of Blood");
    }

    @Test
    @DisplayName("Can destroy a creature controlled by the caster")
    void canDestroyOwnCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RegathanFirecat());
        harness.setHand(player1, List.of(new LiturgyOfBlood()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, creature.getId());

        harness.assertNotOnBattlefield(player1, "Regathan Firecat");
        harness.assertInGraveyard(player1, "Regathan Firecat");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(3);
        harness.assertInGraveyard(player1, "Liturgy of Blood");
    }
}
