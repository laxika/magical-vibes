package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.t.TerramorphicExpanse;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MaelstromArtisanRocketVolley.class, TerramorphicExpanse.class, Mountain.class})
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
        Permanent nonbasicLand = harness.addToBattlefieldAndReturn(player2, new TerramorphicExpanse());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castFromExile(player1, artisan.getPreparedSpellCardId(), nonbasicLand.getId());
        assertThat(artisan.isPrepared()).isFalse();
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

        assertThatThrownBy(() -> harness.castFromExile(player1, artisan.getPreparedSpellCardId(), basicLand.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(artisan.isPrepared()).isTrue();
        assertThat(gd.findExiledCard(artisan.getPreparedSpellCardId())).isNotNull();
    }

    @Test
    void rocketVolleyCannotTargetACreature() {
        Permanent artisan = castMaelstromArtisan();
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castFromExile(player1, artisan.getPreparedSpellCardId(), artisan.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(artisan.isPrepared()).isTrue();
        assertThat(gd.findExiledCard(artisan.getPreparedSpellCardId())).isNotNull();
    }

    @Test
    void rocketVolleyRequiresItsManaCost() {
        Permanent artisan = castMaelstromArtisan();
        Permanent land = harness.addToBattlefieldAndReturn(player2, new TerramorphicExpanse());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, artisan.getPreparedSpellCardId(), land.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(artisan.isPrepared()).isTrue();
        assertThat(gd.findExiledCard(artisan.getPreparedSpellCardId())).isNotNull();
    }

    @Test
    void rocketVolleyCannotBeCastDuringCombat() {
        Permanent artisan = castMaelstromArtisan();
        Permanent land = harness.addToBattlefieldAndReturn(player2, new TerramorphicExpanse());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.castFromExile(player1, artisan.getPreparedSpellCardId(), land.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(artisan.isPrepared()).isTrue();
    }

    @Test
    void rocketVolleyCanDestroyItsControllersNonbasicLandAndItsCopyCeasesToExist() {
        Permanent artisan = castMaelstromArtisan();
        Permanent land = harness.addToBattlefieldAndReturn(player1, new TerramorphicExpanse());
        var copyId = artisan.getPreparedSpellCardId();
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castFromExile(player1, copyId, land.getId());
        assertThat(artisan.isPrepared()).isFalse();
        assertThat(artisan.getPreparedSpellCardId()).isNull();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(land);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(land.getCard());
        assertThat(gd.findExiledCard(copyId)).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(copyId));
    }

    @Test
    void enteringPreparedDoesNotCreateATrigger() {
        harness.castFromHand(player1, new MaelstromArtisanRocketVolley(), "{1}{R}{R}");
        harness.passBothPriorities();

        Permanent artisan = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(artisan.isPrepared()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent castMaelstromArtisan() {
        harness.castFromHand(player1, new MaelstromArtisanRocketVolley(), "{1}{R}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof MaelstromArtisanRocketVolley)
                .findFirst()
                .orElseThrow();
    }
}
