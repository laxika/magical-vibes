package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VodalianZombieTest extends BaseCardTest {

    @Test
    @DisplayName("Vodalian Zombie has protection from green")
    void hasProtectionFromGreen() {
        harness.addToBattlefield(player1, new VodalianZombie());
        Permanent zombie = gd.playerBattlefields.get(player1.getId()).getFirst();

        assertThat(gqs.hasProtectionFrom(gd, zombie, CardColor.GREEN)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, zombie, CardColor.RED)).isFalse();
    }

    @Test
    @DisplayName("Vodalian Zombie cannot be targeted by a green spell")
    void cannotBeTargetedByGreenSpell() {
        harness.addToBattlefield(player2, new VodalianZombie());
        Permanent zombie = gd.playerBattlefields.get(player2.getId()).getFirst();
        harness.setHand(player1, List.of(createTargetedInstant("Green Bolt", CardColor.GREEN)));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, zombie.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from green");
    }

    private static Card createTargetedInstant(String name, CardColor color) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.INSTANT);
        card.setManaCost("{G}");
        card.setColor(color);
        card.addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(1));
        return card;
    }
}
