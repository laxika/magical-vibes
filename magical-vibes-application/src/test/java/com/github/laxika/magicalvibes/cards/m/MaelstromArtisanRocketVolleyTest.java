package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.e.EncroachingWastes;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MaelstromArtisanRocketVolleyTest extends BaseCardTest {

    @Test
    @DisplayName("Maelstrom Artisan enters prepared with a Rocket Volley copy in exile")
    void entersPrepared() {
        Permanent artisan = castMaelstromArtisan();

        assertThat(artisan.isPrepared()).isTrue();
        assertThat(artisan.getPreparedSpellCardId()).isNotNull();
        assertThat(gd.findExiledCard(artisan.getPreparedSpellCardId())).isNotNull();
    }

    @Test
    @DisplayName("Rocket Volley destroys a target nonbasic land and unprepares its source")
    void rocketVolleyDestroysNonbasicLand() {
        Permanent artisan = castMaelstromArtisan();
        Permanent nonbasicLand = harness.addToBattlefieldAndReturn(player2, new EncroachingWastes());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.forceActivePlayer(player1);
        harness.castFromExile(player1, artisan.getPreparedSpellCardId(), nonbasicLand.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(nonbasicLand);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(nonbasicLand.getCard());
        assertThat(artisan.isPrepared()).isFalse();
    }

    @Test
    @DisplayName("Rocket Volley cannot target a basic land")
    void rocketVolleyCannotTargetBasicLand() {
        Permanent artisan = castMaelstromArtisan();
        Permanent basicLand = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.forceActivePlayer(player1);
        assertThatThrownBy(() -> harness.castFromExile(player1, artisan.getPreparedSpellCardId(), basicLand.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent castMaelstromArtisan() {
        harness.setHand(player1, List.of(new MaelstromArtisanRocketVolley()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof MaelstromArtisanRocketVolley)
                .findFirst()
                .orElseThrow();
    }
}
