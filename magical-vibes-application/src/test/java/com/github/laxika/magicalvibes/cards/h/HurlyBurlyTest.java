package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GoldmeadowHarrier;
import com.github.laxika.magicalvibes.cards.k.KinsbaileBalloonist;
import com.github.laxika.magicalvibes.cards.n.NightshadeStinger;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.cards.w.WingsOfVelisVel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HurlyBurly.class, FugitiveWizard.class, SuntailHawk.class,
        GoldmeadowHarrier.class, NightshadeStinger.class, KinsbaileBalloonist.class, WingsOfVelisVel.class})
class HurlyBurlyTest extends BaseCardTest {

    @Test
    @DisplayName("Mode 0 deals 1 damage to each creature without flying only")
    void withoutFlyingModeHitsGroundCreatures() {
        harness.addToBattlefield(player1, new FugitiveWizard()); // 1/1 ground
        harness.addToBattlefield(player2, new SuntailHawk());    // 1/1 flying
        harness.setHand(player1, List.of(new HurlyBurly()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertNotOnBattlefield(player1, "Fugitive Wizard");
        harness.assertOnBattlefield(player2, "Suntail Hawk");
    }

    @Test
    @DisplayName("Mode 1 deals 1 damage to each creature with flying only")
    void withFlyingModeHitsFliers() {
        harness.addToBattlefield(player1, new FugitiveWizard()); // 1/1 ground
        harness.addToBattlefield(player2, new SuntailHawk());    // 1/1 flying
        harness.setHand(player1, List.of(new HurlyBurly()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, 1);

        harness.assertNotOnBattlefield(player2, "Suntail Hawk");
        harness.assertOnBattlefield(player1, "Fugitive Wizard");
    }

    @Test
    @DisplayName("Choosing an invalid mode is rejected at cast time")
    void invalidModeIsRejected() {
        harness.setHand(player1, List.of(new HurlyBurly()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 99))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid mode index");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("Each mode affects both controllers and deals exactly one damage without damaging players")
    void damagesMatchingCreaturesOnBothSides(int mode) {
        for (var player : List.of(player1, player2)) {
            harness.addToBattlefield(player, new GoldmeadowHarrier());
            harness.addToBattlefield(player, new NightshadeStinger());
            harness.addToBattlefield(player, new KinsbaileBalloonist());
        }
        harness.setHand(player1, List.of(new HurlyBurly()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, mode);

        for (var player : List.of(player1, player2)) {
            if (mode == 0) {
                harness.assertNotOnBattlefield(player, "Goldmeadow Harrier");
                harness.assertOnBattlefield(player, "Nightshade Stinger");
            } else {
                harness.assertOnBattlefield(player, "Goldmeadow Harrier");
                harness.assertNotOnBattlefield(player, "Nightshade Stinger");
            }
            harness.assertOnBattlefield(player, "Kinsbaile Balloonist");
            assertThat(findPermanent(player, "Kinsbaile Balloonist").getMarkedDamage()).isEqualTo(mode);
            harness.assertLife(player, 20);
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("Flying granted in response determines which creatures are damaged at resolution")
    void usesFlyingAtResolution(int mode) {
        harness.addToBattlefield(player2, new GoldmeadowHarrier());
        var creature = findPermanent(player2, "Goldmeadow Harrier");
        harness.setHand(player1, List.of(new HurlyBurly()));
        harness.setHand(player2, List.of(new WingsOfVelisVel()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, mode);
        harness.castAndResolveInstant(player2, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Goldmeadow Harrier");
        assertThat(creature.getMarkedDamage()).isEqualTo(mode);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("Either mode can be cast and resolve with no creatures")
    void resolvesOnEmptyBattlefield(int mode) {
        harness.setHand(player1, List.of(new HurlyBurly()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, mode);

        harness.assertInGraveyard(player1, "Hurly-Burly");
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
}
