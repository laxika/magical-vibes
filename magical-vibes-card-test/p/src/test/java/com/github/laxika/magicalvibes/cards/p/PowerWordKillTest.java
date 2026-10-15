package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrinningDemon;
import com.github.laxika.magicalvibes.cards.b.BaneslayerAngel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HulkingDevil;
import com.github.laxika.magicalvibes.cards.t.TwoHeadedDragon;
import com.github.laxika.magicalvibes.cards.t.TreasureVault;
import com.github.laxika.magicalvibes.cards.s.SepulcherGhoul;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PowerWordKill.class, GrizzlyBears.class, BaneslayerAngel.class, GrinningDemon.class,
        HulkingDevil.class, TwoHeadedDragon.class, SepulcherGhoul.class, TreasureVault.class})
class PowerWordKillTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a target creature that is not an Angel, Demon, Devil, or Dragon")
    void destroysValidCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(bears);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Power Word Kill");
    }

    @Test
    @DisplayName("Cannot target an Angel")
    void cannotTargetAngel() {
        assertCannotTarget(new BaneslayerAngel(), "non-Angel");
    }

    @Test
    @DisplayName("Cannot target a Demon")
    void cannotTargetDemon() {
        assertCannotTarget(new GrinningDemon(), "non-Demon");
    }

    @Test
    @DisplayName("Cannot target a Devil")
    void cannotTargetDevil() {
        assertCannotTarget(new HulkingDevil(), "non-Devil");
    }

    @Test
    @DisplayName("Cannot target a Dragon")
    void cannotTargetDragon() {
        assertCannotTarget(new TwoHeadedDragon(), "non-Dragon");
    }

    @Test
    void canDestroyItsControllersCreature() {
        Permanent ghoul = harness.addToBattlefieldAndReturn(player1, new SepulcherGhoul());

        cast(ghoul);

        harness.assertNotOnBattlefield(player1, "Sepulcher Ghoul");
        harness.assertInGraveyard(player1, "Sepulcher Ghoul");
    }

    @Test
    void cannotTargetNoncreaturePermanent() {
        assertCannotTarget(new TreasureVault(), "non-Angel");
    }

    @Test
    void cannotTargetCreatureWithAnAdditionalExcludedSubtype() {
        Permanent ghoul = harness.addToBattlefieldAndReturn(player2, new SepulcherGhoul());
        ghoul.getGrantedSubtypes().add(CardSubtype.DEMON);
        harness.setHand(player1, List.of(new PowerWordKill()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, ghoul.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("non-Demon");
    }

    @Test
    void doesNotDestroyTargetThatGainsExcludedSubtypeBeforeResolution() {
        Permanent ghoul = harness.addToBattlefieldAndReturn(player2, new SepulcherGhoul());
        harness.setHand(player1, List.of(new PowerWordKill()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, ghoul.getId());

        ghoul.getGrantedSubtypes().add(CardSubtype.DRAGON);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Sepulcher Ghoul");
        harness.assertNotInGraveyard(player2, "Sepulcher Ghoul");
        harness.assertInGraveyard(player1, "Power Word Kill");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotDestroyIndestructibleCreature() {
        Permanent ghoul = harness.addToBattlefieldAndReturn(player2, new SepulcherGhoul());
        ghoul.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);

        cast(ghoul);

        harness.assertOnBattlefield(player2, "Sepulcher Ghoul");
        harness.assertNotInGraveyard(player2, "Sepulcher Ghoul");
        harness.assertInGraveyard(player1, "Power Word Kill");
    }

    @Test
    void allowsRegeneration() {
        Permanent ghoul = harness.addToBattlefieldAndReturn(player2, new SepulcherGhoul());
        ghoul.setRegenerationShield(1);

        cast(ghoul);

        harness.assertOnBattlefield(player2, "Sepulcher Ghoul");
        harness.assertNotInGraveyard(player2, "Sepulcher Ghoul");
        assertThat(ghoul.isTapped()).isTrue();
        assertThat(ghoul.getRegenerationShield()).isZero();
        harness.assertInGraveyard(player1, "Power Word Kill");
    }

    private void cast(Permanent target) {
        harness.setHand(player1, List.of(new PowerWordKill()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private void assertCannotTarget(Card card, String message) {
        Permanent target = harness.addToBattlefieldAndReturn(player2, card);

        harness.setHand(player1, List.of(new PowerWordKill()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(message);
    }
}
