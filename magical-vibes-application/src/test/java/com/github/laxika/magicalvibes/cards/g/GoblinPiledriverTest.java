package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GoblinPiledriverTest extends BaseCardTest {

    private static Card createTargetedInstant(String name, CardColor color, String manaCost) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.INSTANT);
        card.setManaCost(manaCost);
        card.setColor(color);
        card.addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(1));
        return card;
    }

    private Permanent addPiledriver(Player player) {
        Permanent piledriver = harness.addToBattlefieldAndReturn(player, new GoblinPiledriver());
        piledriver.setSummoningSick(false);
        return piledriver;
    }

    private Permanent addGoblin(Player player) {
        Permanent piker = new Permanent(new GoblinPiker());
        piker.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(piker);
        return piker;
    }

    @Test
    @DisplayName("Gets +2/+0 for each other attacking Goblin")
    void boostScalesWithOtherAttackingGoblins() {
        Permanent piledriver = addPiledriver(player1);
        addGoblin(player1);
        addGoblin(player1);

        declareAttackers(List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(piledriver.getPowerModifier()).isEqualTo(4);
        assertThat(piledriver.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Attacking alone gives no boost (itself is not an 'other' Goblin)")
    void noBoostWhenAttackingAlone() {
        Permanent piledriver = addPiledriver(player1);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(piledriver.getPowerModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Non-Goblin attackers do not increase the boost")
    void nonGoblinAttackersNotCounted() {
        Permanent piledriver = addPiledriver(player1);
        addGoblin(player1);
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        declareAttackers(List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(piledriver.getPowerModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("A Goblin that stays home does not increase the boost")
    void nonAttackingGoblinNotCounted() {
        Permanent piledriver = addPiledriver(player1);
        addGoblin(player1);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(piledriver.getPowerModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot be targeted by a blue instant")
    void cannotBeTargetedByBlueInstant() {
        Permanent piledriver = addPiledriver(player2);
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        harness.setHand(player1, List.of(createTargetedInstant("Blue Zap", CardColor.BLUE, "{U}")));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, piledriver.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from blue");
    }

    @Test
    @DisplayName("Can be targeted by a red instant")
    void canBeTargetedByRedInstant() {
        Permanent piledriver = addPiledriver(player1);

        harness.setHand(player1, List.of(createTargetedInstant("Red Zap", CardColor.RED, "{R}")));
        harness.addMana(player1, ManaColor.RED, 1);

        gs.playCard(gd, player1, 0, 0, piledriver.getId(), null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Red Zap");
    }
}
