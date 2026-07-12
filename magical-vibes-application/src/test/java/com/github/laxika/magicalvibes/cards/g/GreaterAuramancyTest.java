package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerrasEmbrace;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GreaterAuramancyTest extends BaseCardTest {

    private Permanent attachAura(Permanent creature, java.util.UUID controllerId) {
        Permanent aura = new Permanent(new SerrasEmbrace());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(controllerId).add(aura);
        return aura;
    }

    @Test
    @DisplayName("Other enchantments you control have shroud")
    void otherEnchantmentsYouControlHaveShroud() {
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new GreaterAuramancy()));
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);
        Permanent aura = attachAura(bears, player1.getId());

        assertThat(gqs.hasKeyword(gd, aura, Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("Enchanted creatures you control have shroud")
    void enchantedCreaturesYouControlHaveShroud() {
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new GreaterAuramancy()));
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);
        attachAura(bears, player1.getId());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("Greater Auramancy does not grant shroud to itself")
    void doesNotGrantShroudToItself() {
        Permanent auramancy = new Permanent(new GreaterAuramancy());
        gd.playerBattlefields.get(player1.getId()).add(auramancy);

        assertThat(gqs.hasKeyword(gd, auramancy, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("An unenchanted creature you control does not have shroud")
    void unenchantedCreatureHasNoShroud() {
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new GreaterAuramancy()));
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Enchantments and enchanted creatures you do not control are unaffected")
    void doesNotAffectPermanentsYouDoNotControl() {
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new GreaterAuramancy()));
        Permanent enemyBears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(enemyBears);
        Permanent enemyAura = attachAura(enemyBears, player2.getId());

        assertThat(gqs.hasKeyword(gd, enemyBears, Keyword.SHROUD)).isFalse();
        assertThat(gqs.hasKeyword(gd, enemyAura, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Shroud is lost once Greater Auramancy leaves the battlefield")
    void shroudLostWhenAuramancyRemoved() {
        Permanent auramancy = new Permanent(new GreaterAuramancy());
        gd.playerBattlefields.get(player1.getId()).add(auramancy);
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);
        Permanent aura = attachAura(bears, player1.getId());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.SHROUD)).isTrue();
        assertThat(gqs.hasKeyword(gd, aura, Keyword.SHROUD)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(auramancy);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.SHROUD)).isFalse();
        assertThat(gqs.hasKeyword(gd, aura, Keyword.SHROUD)).isFalse();
    }
}
