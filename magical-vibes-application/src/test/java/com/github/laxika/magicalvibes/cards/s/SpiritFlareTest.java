package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CentaurVeteran;
import com.github.laxika.magicalvibes.cards.t.TerohsFaithful;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpiritFlare.class, CentaurVeteran.class, TerohsFaithful.class})
class SpiritFlareTest extends BaseCardTest {

    @Test
    @DisplayName("Taps your creature and deals damage equal to its power to an attacking creature")
    void tapsYourCreatureAndDamagesAttacker() {
        Permanent source = addCreatureReady(player1, new CentaurVeteran());
        Permanent attacker = addCreatureReady(player2, new TerohsFaithful());
        attacker.setAttacking(true);
        castFromHand(source, attacker);

        harness.passBothPriorities();

        assertThat(source.isTapped()).isTrue();
        assertThat(attacker.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Deals damage to a blocking creature")
    void damagesBlocker() {
        Permanent source = addCreatureReady(player1, new CentaurVeteran());
        Permanent blocker = addCreatureReady(player2, new TerohsFaithful());
        blocker.setBlocking(true);
        castFromHand(source, blocker);

        harness.passBothPriorities();

        assertThat(source.isTapped()).isTrue();
        assertThat(blocker.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not deal damage when the source is tapped before resolution")
    void doesNotDamageWhenSourceBecomesTapped() {
        Permanent source = addCreatureReady(player1, new CentaurVeteran());
        Permanent attacker = addCreatureReady(player2, new TerohsFaithful());
        attacker.setAttacking(true);
        castFromHand(source, attacker);

        source.tap();
        harness.passBothPriorities();

        assertThat(source.isTapped()).isTrue();
        assertThat(attacker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Does not deal damage when the first target leaves before resolution")
    void doesNotDamageWhenFirstTargetLeaves() {
        Permanent source = addCreatureReady(player1, new CentaurVeteran());
        Permanent attacker = addCreatureReady(player2, new TerohsFaithful());
        attacker.setAttacking(true);
        castFromHand(source, attacker);

        gd.playerBattlefields.get(player1.getId()).remove(source);
        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Taps the first target but deals no damage when the second target leaves")
    void tapsFirstTargetWhenSecondTargetLeaves() {
        Permanent source = addCreatureReady(player1, new CentaurVeteran());
        Permanent attacker = addCreatureReady(player2, new TerohsFaithful());
        attacker.setAttacking(true);
        castFromHand(source, attacker);

        gd.playerBattlefields.get(player2.getId()).remove(attacker);
        harness.passBothPriorities();

        assertThat(source.isTapped()).isTrue();
        assertThat(attacker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Cannot target a tapped creature you control as the first target")
    void cannotTargetTappedFirstCreature() {
        Permanent source = addCreatureReady(player1, new CentaurVeteran());
        source.tap();
        Permanent attacker = addCreatureReady(player2, new TerohsFaithful());
        attacker.setAttacking(true);

        assertThatThrownBy(() -> castFromHand(source, attacker))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("untapped creature you control");
    }

    @Test
    @DisplayName("Cannot target an opponent's creature as the first target")
    void cannotTargetOpponentCreatureFirst() {
        Permanent opponentCreature = addCreatureReady(player2, new CentaurVeteran());
        Permanent attacker = addCreatureReady(player2, new TerohsFaithful());
        attacker.setAttacking(true);

        assertThatThrownBy(() -> castFromHand(opponentCreature, attacker))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    @Test
    @DisplayName("Cannot target a player as the first target")
    void cannotTargetPlayerFirst() {
        Permanent attacker = addCreatureReady(player2, new TerohsFaithful());
        attacker.setAttacking(true);
        prepareSpell();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(player1.getId(), attacker.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target your own creature as the second target")
    void cannotTargetOwnCreatureSecond() {
        Permanent source = addCreatureReady(player1, new CentaurVeteran());
        Permanent ownCombatCreature = addCreatureReady(player1, new TerohsFaithful());
        ownCombatCreature.setAttacking(true);

        assertThatThrownBy(() -> castFromHand(source, ownCombatCreature))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent controls");
    }

    @Test
    @DisplayName("Cannot target a nonattacking and nonblocking creature as the second target")
    void cannotTargetNonCombatCreature() {
        Permanent source = addCreatureReady(player1, new CentaurVeteran());
        Permanent bystander = addCreatureReady(player2, new TerohsFaithful());

        assertThatThrownBy(() -> castFromHand(source, bystander))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking or blocking creature");
    }

    @Test
    @DisplayName("Flashback pays three life, resolves, and exiles Spirit Flare")
    void flashbackPaysLifeResolvesAndExiles() {
        Permanent source = addCreatureReady(player1, new CentaurVeteran());
        Permanent attacker = addCreatureReady(player2, new TerohsFaithful());
        attacker.setAttacking(true);
        harness.setGraveyard(player1, List.of(new SpiritFlare()));
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFlashback(player1, 0, List.of(source.getId(), attacker.getId()));
        harness.passBothPriorities();

        harness.assertLife(player1, 7);
        assertThat(source.isTapped()).isTrue();
        assertThat(attacker.getMarkedDamage()).isEqualTo(3);
        harness.assertNotInGraveyard(player1, "Spirit Flare");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Spirit Flare"));
    }

    private void castFromHand(Permanent source, Permanent target) {
        prepareSpell();
        harness.castInstant(player1, 0, List.of(source.getId(), target.getId()));
    }

    private void prepareSpell() {
        harness.setHand(player1, List.of(new SpiritFlare()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
