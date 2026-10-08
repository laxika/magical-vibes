package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UnsparingBoltcaster.class, AvatarOfMight.class, GrizzlyBears.class, Shock.class})
class UnsparingBoltcasterTest extends BaseCardTest {

    @Test
    @DisplayName("ETB deals 5 damage to an opponent's creature dealt damage this turn")
    void etbDealsFiveDamageToDamagedOpponentCreature() {
        Permanent target = addCreatureReady(player2, new AvatarOfMight());
        UUID targetId = target.getId();
        gd.permanentsDealtDamageThisTurn.add(targetId);

        harness.setHand(player1, List.of(new UnsparingBoltcaster()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castCreature(player1, 0, targetId);

        resolveAllTriggers();

        assertThat(findPermanent(player2, "Avatar of Might").getMarkedDamage()).isEqualTo(5);
    }

    @Test
    @DisplayName("Cannot target an opponent's creature that was not dealt damage this turn")
    void cannotTargetUndamagedCreature() {
        UUID targetId = addCreatureReady(player2, new GrizzlyBears()).getId();

        harness.setHand(player1, List.of(new UnsparingBoltcaster()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("dealt damage this turn");
    }

    @Test
    @DisplayName("Cannot target a damaged creature controlled by its controller")
    void cannotTargetOwnCreature() {
        UUID targetId = addCreatureReady(player1, new GrizzlyBears()).getId();
        gd.permanentsDealtDamageThisTurn.add(targetId);

        harness.setHand(player1, List.of(new UnsparingBoltcaster()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent controls");
    }

    @Test
    @DisplayName("Creature enters and its trigger leaves the stack when no legal target exists")
    void creatureEntersWithoutValidTarget() {
        harness.setHand(player1, List.of(new UnsparingBoltcaster()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Actual spell damage makes an opposing creature a legal target")
    void etbRecognizesDamageDealtBySpell() {
        Permanent target = addCreatureReady(player2, new AvatarOfMight());
        harness.setHand(player1, List.of(new Shock(), new UnsparingBoltcaster()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.castCreature(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(target.getMarkedDamage()).isEqualTo(7);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("ETB kills an opposing creature with less than five remaining toughness")
    void etbDealsLethalDamage() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        gd.permanentsDealtDamageThisTurn.add(target.getId());
        target.setMarkedDamage(1);
        harness.setHand(player1, List.of(new UnsparingBoltcaster()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(target.getCard());
    }

    @Test
    @DisplayName("ETB cannot damage a target that its controller gains control of in response")
    void targetMustStillBeControlledByOpponentOnResolution() {
        Permanent target = addCreatureReady(player2, new AvatarOfMight());
        gd.permanentsDealtDamageThisTurn.add(target.getId());
        harness.setHand(player1, List.of(new UnsparingBoltcaster()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerBattlefields.get(player1.getId()).add(target);
        resolveAllTriggers();

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ETB still deals damage after Boltcaster dies in response")
    void triggerResolvesAfterSourceDies() {
        Permanent target = addCreatureReady(player2, new AvatarOfMight());
        gd.permanentsDealtDamageThisTurn.add(target.getId());
        harness.setHand(player1, List.of(new UnsparingBoltcaster(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        Permanent source = findPermanent(player1, "Unsparing Boltcaster");
        harness.castAndResolveInstant(player1, 0, source.getId());
        harness.castAndResolveInstant(player1, 0, source.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(source);
        resolveAllTriggers();

        assertThat(target.getMarkedDamage()).isEqualTo(5);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(source.getCard());
    }
}
