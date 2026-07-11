package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SunspearShikariTest extends BaseCardTest {

    // ===== Without equipment =====

    @Test
    @DisplayName("Without equipment, does not have first strike or lifelink")
    void withoutEquipmentNoKeywords() {
        Permanent shikari = addShikariReady(player1);

        assertThat(gqs.hasKeyword(gd, shikari, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, shikari, Keyword.LIFELINK)).isFalse();
    }

    // ===== With equipment =====

    @Test
    @DisplayName("With equipment attached, has first strike and lifelink")
    void withEquipmentHasKeywords() {
        Permanent shikari = addShikariReady(player1);
        Permanent scimitar = addScimitarReady(player1);
        scimitar.setAttachedTo(shikari.getId());

        assertThat(gqs.hasKeyword(gd, shikari, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, shikari, Keyword.LIFELINK)).isTrue();
    }

    // ===== Equipment on other creature doesn't grant keywords =====

    @Test
    @DisplayName("Equipment on another creature does not grant keywords to Shikari")
    void equipmentOnOtherCreatureDoesNotGrantKeywords() {
        Permanent shikari = addShikariReady(player1);
        Permanent other = addShikariReady(player1);
        Permanent scimitar = addScimitarReady(player1);
        scimitar.setAttachedTo(other.getId());

        assertThat(gqs.hasKeyword(gd, shikari, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, shikari, Keyword.LIFELINK)).isFalse();
    }

    // ===== Unattached equipment doesn't grant keywords =====

    @Test
    @DisplayName("Unattached equipment on battlefield does not grant keywords")
    void unattachedEquipmentDoesNotGrantKeywords() {
        Permanent shikari = addShikariReady(player1);
        addScimitarReady(player1);

        assertThat(gqs.hasKeyword(gd, shikari, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, shikari, Keyword.LIFELINK)).isFalse();
    }

    // ===== Equipment removed loses keywords =====

    @Test
    @DisplayName("After equipment is detached, loses first strike and lifelink")
    void afterEquipmentDetachedLosesKeywords() {
        Permanent shikari = addShikariReady(player1);
        Permanent scimitar = addScimitarReady(player1);
        scimitar.setAttachedTo(shikari.getId());

        assertThat(gqs.hasKeyword(gd, shikari, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, shikari, Keyword.LIFELINK)).isTrue();

        scimitar.setAttachedTo(null);

        assertThat(gqs.hasKeyword(gd, shikari, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, shikari, Keyword.LIFELINK)).isFalse();
    }

    // ===== Opponent's equipment still grants keywords =====

    @Test
    @DisplayName("Opponent's equipment attached to Shikari still grants keywords")
    void opponentEquipmentGrantsKeywords() {
        Permanent shikari = addShikariReady(player1);
        Permanent scimitar = addScimitarReady(player2);
        scimitar.setAttachedTo(shikari.getId());

        assertThat(gqs.hasKeyword(gd, shikari, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, shikari, Keyword.LIFELINK)).isTrue();
    }

    // ===== Helpers =====

    private Permanent addShikariReady(Player player) {
        Permanent perm = new Permanent(new SunspearShikari());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addScimitarReady(Player player) {
        Permanent perm = new Permanent(new LeoninScimitar());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
