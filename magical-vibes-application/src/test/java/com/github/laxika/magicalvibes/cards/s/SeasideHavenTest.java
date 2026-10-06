package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.cards.a.ArtificialEvolution;
import com.github.laxika.magicalvibes.cards.b.Bitterblossom;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SeasideHaven.class, SageAven.class, GlorySeeker.class})
class SeasideHavenTest extends BaseCardTest {

    @Test
    void tapsForColorlessMana() {
        Permanent haven = addCreatureReady(player1, new SeasideHaven());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(haven.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void sacrificesBirdAndDrawsCard() {
        addCreatureReady(player1, new SeasideHaven());
        harness.addToBattlefield(player1, new SageAven());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);

        harness.assertInGraveyard(player1, "Sage Aven");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(findPermanent(player1, "Seaside Haven").isTapped()).isTrue();
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(gameData.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        harness.assertInGraveyard(player1, "Sage Aven");
        harness.assertOnBattlefield(player1, "Seaside Haven");
    }

    @Test
    void drawAbilityCannotSacrificeNonBirdCreature() {
        addCreatureReady(player1, new SeasideHaven());
        harness.addToBattlefield(player1, new GlorySeeker());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canSacrificeTappedSummoningSickBird() {
        addCreatureReady(player1, new SeasideHaven());
        Permanent bird = harness.addToBattlefieldAndReturn(player1, new SageAven());
        bird.tap();
        bird.setSummoningSick(true);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setLibrary(player1, List.of(new GlorySeeker()));
        harness.setHand(player1, List.of());

        harness.activateAbility(player1, 0, 1, null, null);

        harness.assertInGraveyard(player1, "Sage Aven");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        harness.passBothPriorities();
        harness.assertInHand(player1, "Glory Seeker");
    }

    @Test
    void cannotSacrificeOpponentsBird() {
        Permanent haven = addCreatureReady(player1, new SeasideHaven());
        harness.addToBattlefield(player2, new SageAven());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(haven.isTapped()).isFalse();
        harness.assertOnBattlefield(player2, "Sage Aven");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotPayColoredCostWithOnlyColorlessMana() {
        Permanent haven = addCreatureReady(player1, new SeasideHaven());
        harness.addToBattlefield(player1, new SageAven());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(haven.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Sage Aven");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void tappedHavenCannotActivateEitherAbility() {
        Permanent haven = addCreatureReady(player1, new SeasideHaven());
        haven.tap();
        harness.addToBattlefield(player1, new SageAven());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Sage Aven");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed({ArtificialEvolution.class, Bitterblossom.class})
    void canSacrificeNoncreatureKindredBird() {
        addCreatureReady(player1, new SeasideHaven());
        Permanent blossom = harness.addToBattlefieldAndReturn(player1, new Bitterblossom());
        harness.setHand(player1, List.of(new ArtificialEvolution()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, blossom.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "FAERIE");
        harness.handleListChoice(player1, "BIRD");
        assertThat(gqs.hasEffectiveSubtype(gd, blossom, CardSubtype.BIRD)).isTrue();
        harness.setLibrary(player1, List.of(new GlorySeeker()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        harness.assertInGraveyard(player1, "Bitterblossom");
        harness.passBothPriorities();
        harness.assertInHand(player1, "Glory Seeker");
    }

    private Permanent addReadyHaven(Player player) {
        Permanent haven = harness.addToBattlefieldAndReturn(player, new SeasideHaven());
        haven.setSummoningSick(false);
        return haven;
    }
}
