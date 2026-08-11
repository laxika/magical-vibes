package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MysticEnforcerTest extends BaseCardTest {

    @Test
    @DisplayName("Remains 3/3 without threshold")
    void noThresholdBonusBelowSevenCards() {
        fillGraveyard(player1, 6);
        Permanent enforcer = addEnforcer(player1);

        assertThat(gqs.getEffectivePower(gd, enforcer)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, enforcer)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, enforcer, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Gets +3/+3 and flying at threshold")
    void thresholdBonusAtSevenCards() {
        fillGraveyard(player1, 7);
        Permanent enforcer = addEnforcer(player1);

        assertThat(gqs.getEffectivePower(gd, enforcer)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, enforcer)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, enforcer, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Only its controller's graveyard counts")
    void opponentGraveyardDoesNotCount() {
        fillGraveyard(player2, 7);
        Permanent enforcer = addEnforcer(player1);

        assertThat(gqs.getEffectivePower(gd, enforcer)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, enforcer, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Protection from black prevents black spells from targeting it")
    void protectionFromBlackPreventsBlackTargeting() {
        Permanent enforcer = addEnforcer(player1);
        addCreature(player1, new GrizzlyBears());

        harness.setHand(player2, List.of(createTargetedInstant("Black Bolt", CardColor.BLACK, "{B}")));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.passPriority(player1);

        assertThatThrownBy(() -> gs.playCard(gd, player2, 0, 0, enforcer.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from black");
    }

    private Permanent addEnforcer(Player player) {
        return addCreature(player, new MysticEnforcer());
    }

    private Permanent addCreature(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void fillGraveyard(Player player, int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new GrizzlyBears());
        }
        harness.setGraveyard(player, cards);
    }

    private static Card createTargetedInstant(String name, CardColor color, String manaCost) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.INSTANT);
        card.setManaCost(manaCost);
        card.setColor(color);
        card.addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(1));
        return card;
    }
}
