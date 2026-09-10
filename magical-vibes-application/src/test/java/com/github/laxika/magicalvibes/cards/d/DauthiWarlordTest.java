package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DauthiWarlord.class, DauthiCutthroat.class, RagingGoblin.class})
class DauthiWarlordTest extends BaseCardTest {

    @Test
    @DisplayName("Dauthi Warlord's power is the number of shadow creatures on the battlefield")
    void powerCountsShadowCreaturesOnBattlefield() {
        Permanent warlord = addWarlordReady(player1);
        addShadowCreature(player1);
        addShadowCreature(player2);
        addCreatureReady(player2, new RagingGoblin());

        assertThat(gqs.getEffectivePower(gd, warlord)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, warlord)).isEqualTo(1);
    }

    @Test
    @DisplayName("Dauthi Warlord ignores noncreatures with shadow")
    void ignoresNoncreaturesWithShadow() {
        Permanent warlord = addWarlordReady(player1);
        DauthiCutthroat enchantment = new DauthiCutthroat();
        enchantment.setType(CardType.ENCHANTMENT);
        gd.playerBattlefields.get(player2.getId()).add(new Permanent(enchantment));

        assertThat(gqs.getEffectivePower(gd, warlord)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, warlord)).isEqualTo(1);
    }

    @Test
    @DisplayName("Dauthi Warlord counts shadow creatures only on the battlefield")
    void ignoresShadowCreaturesOutsideBattlefield() {
        Permanent warlord = addWarlordReady(player1);
        harness.setHand(player1, List.of(new DauthiCutthroat()));
        harness.setGraveyard(player1, List.of(new DauthiCutthroat()));
        harness.setExile(player2, List.of(new DauthiCutthroat()));

        assertThat(gqs.getEffectivePower(gd, warlord)).isEqualTo(1);
    }

    @Test
    @DisplayName("Dauthi Warlord's power updates as shadow creatures enter and leave")
    void powerUpdatesAsShadowCreaturesChange() {
        Permanent warlord = addWarlordReady(player1);

        assertThat(gqs.getEffectivePower(gd, warlord)).isEqualTo(1);

        Permanent shadowCreature = addShadowCreature(player2);

        assertThat(gqs.getEffectivePower(gd, warlord)).isEqualTo(2);

        gd.playerBattlefields.get(player2.getId()).remove(shadowCreature);

        assertThat(gqs.getEffectivePower(gd, warlord)).isEqualTo(1);
    }

    private Permanent addWarlordReady(Player player) {
        return addCreatureReady(player, new DauthiWarlord());
    }

    private Permanent addShadowCreature(Player player) {
        return addCreatureReady(player, new DauthiCutthroat());
    }
}
