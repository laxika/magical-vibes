package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.b.BoggartBrute;
import com.github.laxika.magicalvibes.cards.f.FaerieMiscreant;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.s.SoulWarden;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PracticedTactics.class, AirElemental.class, BoggartBrute.class, FaerieMiscreant.class,
        FugitiveWizard.class, SoulWarden.class})
class PracticedTacticsTest extends BaseCardTest {

    @Test
    @DisplayName("Deals twice the size of your party to an attacking creature")
    void dealsTwicePartySizeToAttacker() {
        addFullParty();
        Permanent target = addAttacker(player2, tenTenCreature());

        castAt(target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(8);
    }

    @Test
    @DisplayName("A creature with two party types fills only one role")
    void oneCreatureCannotFillTwoPartyRoles() {
        harness.addToBattlefield(player1,
                partyCreature("Cleric Rogue", CardSubtype.CLERIC, CardSubtype.ROGUE));
        harness.addToBattlefield(player1, new BoggartBrute());
        harness.addToBattlefield(player1, new FugitiveWizard());
        Permanent target = addBlocker(player2, tenTenCreature());

        castAt(target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(6);
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking or blocking")
    void cannotTargetNonCombatCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new PracticedTactics()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking or blocking creature");
    }

    private void addFullParty() {
        harness.addToBattlefield(player1, new SoulWarden());
        harness.addToBattlefield(player1, new FaerieMiscreant());
        harness.addToBattlefield(player1, new BoggartBrute());
        harness.addToBattlefield(player1, new FugitiveWizard());
    }

    private void castAt(UUID targetId) {
        harness.setHand(player1, List.of(new PracticedTactics()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }

    private Permanent addAttacker(Player owner, Card card) {
        harness.addToBattlefield(owner, card);
        Permanent attacker = findPermanent(owner, card.getName());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());
        return attacker;
    }

    private Permanent addBlocker(Player owner, Card card) {
        harness.addToBattlefield(owner, card);
        Permanent blocker = findPermanent(owner, card.getName());
        blocker.setBlocking(true);
        blocker.addBlockingTargetId(UUID.randomUUID());
        return blocker;
    }

    private Card tenTenCreature() {
        Card card = new Card();
        card.setName("Ten-Ten Creature");
        card.setType(CardType.CREATURE);
        card.setPower(10);
        card.setToughness(10);
        return card;
    }

    private Card partyCreature(String name, CardSubtype... subtypes) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setSubtypes(List.of(subtypes));
        card.setPower(2);
        card.setToughness(2);
        return card;
    }
}
