package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
import com.github.laxika.magicalvibes.cards.w.WurmcoilEngine;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TheMightstoneAndWeakstoneTest extends BaseCardTest {

    @Test
    @DisplayName("The Mightstone and Weakstone's draw mode draws two cards")
    void drawModeDrawsTwoCards() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        cast(0, null);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("The Mightstone and Weakstone's debuff mode gives a creature -5/-5")
    void debuffModeGivesTargetCreatureMinusFiveMinusFive() {
        Permanent target = addCreatureReady(player1, new WurmcoilEngine());
        cast(1, target.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }

    @Test
    @DisplayName("The debuff mode rejects a noncreature target")
    void debuffModeRejectsNoncreatureTarget() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new com.github.laxika.magicalvibes.cards.f.Forest());
        harness.setHand(player1, List.of(new TheMightstoneAndWeakstone()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 1, land.getId(), null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The tap ability produces two Powerstone-restricted colorless mana")
    void tapAbilityProducesPowerstoneMana() {
        Permanent mightstone = harness.addToBattlefieldAndReturn(player1, new TheMightstoneAndWeakstone());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getPowerstoneOnlyColorless()).isEqualTo(2);
        assertThat(mightstone.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Powerstone mana can cast artifacts but not nonartifact spells")
    void powerstoneManaRestrictsNonartifactSpells() {
        harness.addToBattlefield(player1, new TheMightstoneAndWeakstone());
        harness.addToBattlefield(player1, new TheMightstoneAndWeakstone());
        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 1, null, null);

        harness.setHand(player1, List.of(new PropheticPrism()));
        harness.castArtifact(player1, 0);
        assertThat(gd.stack).hasSize(1);

        Card nonartifact = new Card();
        nonartifact.setName("Generic Creature");
        nonartifact.setType(CardType.CREATURE);
        nonartifact.setManaCost("{2}");
        nonartifact.setPower(2);
        nonartifact.setToughness(2);
        harness.setHand(player1, List.of(nonartifact));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(int mode, java.util.UUID targetId) {
        harness.setHand(player1, List.of(new TheMightstoneAndWeakstone()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        gs.playCard(gd, player1, 0, mode, targetId, null);
    }
}
