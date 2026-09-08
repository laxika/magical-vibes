package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TeamPennantTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +1/+1 and vigilance and trample")
    void equippedCreatureGetsBoostAndKeywords() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent pennant = addPennantReady(player1);
        pennant.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Equip creature token {1} attaches Team Pennant to a creature token")
    void tokenEquipAttachesToCreatureToken() {
        Permanent pennant = addPennantReady(player1);
        Permanent token = addTokenCreature(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, token.getId());
        harness.passBothPriorities();

        assertThat(pennant.getAttachedTo()).isEqualTo(token.getId());
    }

    @Test
    @DisplayName("Equip creature token {1} cannot target a nontoken creature")
    void tokenEquipRejectsNontokenCreature() {
        Permanent pennant = addPennantReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature token you control");
        assertThat(pennant.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Equip {3} attaches Team Pennant to a nontoken creature")
    void regularEquipAttachesToNontokenCreature() {
        Permanent pennant = addPennantReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(pennant.getAttachedTo()).isEqualTo(creature.getId());
    }

    private Permanent addPennantReady(Player player) {
        Permanent permanent = new Permanent(new TeamPennant());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addTokenCreature(Player player) {
        GrizzlyBears tokenCard = new GrizzlyBears();
        tokenCard.setToken(true);
        return addCreatureReady(player, tokenCard);
    }
}
