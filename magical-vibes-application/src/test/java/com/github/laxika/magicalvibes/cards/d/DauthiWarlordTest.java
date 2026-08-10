package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class DauthiWarlordTest extends BaseCardTest {

    @Test
    @DisplayName("Dauthi Warlord's power is the number of shadow creatures on the battlefield")
    void powerCountsShadowCreaturesOnBattlefield() {
        Permanent warlord = addWarlordReady(player1);
        addShadowCreature(player1);
        addShadowCreature(player2);
        addCreatureReady(player2, creatureCard("Non-shadow creature", Set.of()));

        assertThat(gqs.getEffectivePower(gd, warlord)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, warlord)).isEqualTo(1);
    }

    @Test
    @DisplayName("Dauthi Warlord ignores noncreatures with shadow")
    void ignoresNoncreaturesWithShadow() {
        Permanent warlord = addWarlordReady(player1);
        Card enchantment = new Card();
        enchantment.setName("Shadow Enchantment");
        enchantment.setType(CardType.ENCHANTMENT);
        enchantment.setKeywords(Set.of(Keyword.SHADOW));
        gd.playerBattlefields.get(player2.getId()).add(new Permanent(enchantment));

        assertThat(gqs.getEffectivePower(gd, warlord)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, warlord)).isEqualTo(1);
    }

    @Test
    @DisplayName("Dauthi Warlord's power updates as shadow creatures enter and leave")
    void powerUpdatesAsShadowCreaturesChange() {
        Permanent warlord = addWarlordReady(player1);
        Permanent shadowCreature = addShadowCreature(player2);

        assertThat(gqs.getEffectivePower(gd, warlord)).isEqualTo(2);

        gd.playerBattlefields.get(player2.getId()).remove(shadowCreature);

        assertThat(gqs.getEffectivePower(gd, warlord)).isEqualTo(1);
    }

    private Permanent addWarlordReady(Player player) {
        return addCreatureReady(player, new DauthiWarlord());
    }

    private Permanent addShadowCreature(Player player) {
        return addCreatureReady(player, creatureCard("Shadow Creature", Set.of(Keyword.SHADOW)));
    }

    private Card creatureCard(String name, Set<Keyword> keywords) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setPower(2);
        card.setToughness(2);
        card.setKeywords(keywords);
        return card;
    }
}
