package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BloodthroneVampire;
import com.github.laxika.magicalvibes.cards.b.BarterInBlood;
import com.github.laxika.magicalvibes.cards.d.DrudgeSkeletons;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Hatred;
import com.github.laxika.magicalvibes.cards.k.KrumarInitiate;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AngelOfJubilation.class, BloodthroneVampire.class, DrudgeSkeletons.class,
        GrizzlyBears.class, LlanowarElves.class, AdantoVanguard.class, AltarsReap.class,
        BarterInBlood.class, Hatred.class, KrumarInitiate.class})
class AngelOfJubilationTest extends BaseCardTest {

    @Test
    @DisplayName("Other nonblack creatures you control get +1/+1")
    void boostsOtherNonblackCreaturesYouControl() {
        addCreatureReady(player1, new AngelOfJubilation());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Black creatures and the Angel itself are not boosted")
    void doesNotBoostBlackCreaturesOrItself() {
        Permanent angel = addCreatureReady(player1, new AngelOfJubilation());
        Permanent skeletons = addCreatureReady(player1, new DrudgeSkeletons());

        assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, skeletons)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, skeletons)).isEqualTo(1);
    }

    @Test
    @DisplayName("Opponent's nonblack creatures are not boosted")
    void doesNotBoostOpponentCreatures() {
        addCreatureReady(player1, new AngelOfJubilation());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Players can't pay life to activate an ability")
    void cantPayLifeToActivateAbility() {
        addCreatureReady(player1, new AngelOfJubilation());
        addCreatureReady(player1, new AdantoVanguard());
        harness.setLife(player1, 20);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("pay life");

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Players can't sacrifice creatures to activate an ability")
    void cantSacrificeCreatureToActivateAbility() {
        addCreatureReady(player1, new AngelOfJubilation());
        addCreatureReady(player1, new BloodthroneVampire());
        addCreatureReady(player1, new LlanowarElves());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice creatures");

        harness.assertOnBattlefield(player1, "Llanowar Elves");
    }

    @Test
    @DisplayName("Players can't sacrifice creatures to cast a spell")
    void cantSacrificeCreatureToCastSpell() {
        addCreatureReady(player1, new AngelOfJubilation());
        Permanent sacrifice = addCreatureReady(player1, new LlanowarElves());

        harness.setHand(player1, List.of(new AltarsReap()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, null, sacrifice.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice creatures");

        harness.assertOnBattlefield(player1, "Llanowar Elves");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Restriction also applies to the opponent")
    void restrictionAppliesToOpponent() {
        addCreatureReady(player1, new AngelOfJubilation());
        addCreatureReady(player2, new AdantoVanguard());
        harness.setLife(player2, 20);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("pay life");
    }

    @Test
    @DisplayName("Sacrifice costs work normally once the Angel leaves the battlefield")
    void sacrificeAllowedWithoutAngel() {
        Permanent sacrifice = addCreatureReady(player1, new LlanowarElves());

        harness.setHand(player1, List.of(new AltarsReap()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstantWithSacrifice(player1, 0, null, sacrifice.getId());

        assertThat(gd.stack).hasSize(1);
        harness.assertInGraveyard(player1, "Llanowar Elves");
    }

    @Test
    @DisplayName("Two Angels boost each other but not themselves")
    void angelsBoostEachOther() {
        Permanent first = addCreatureReady(player1, new AngelOfJubilation());
        Permanent second = addCreatureReady(player1, new AngelOfJubilation());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(4);
    }

    @Test
    @DisplayName("A resolving spell can sacrifice creatures despite the Angel")
    void resolvingSpellCanSacrificeCreatures() {
        addCreatureReady(player1, new AngelOfJubilation());
        addCreatureReady(player1, new AngelOfJubilation());
        addCreatureReady(player2, new AngelOfJubilation());
        addCreatureReady(player2, new AngelOfJubilation());

        harness.castFromHand(player1, new BarterInBlood(), "{2}{B}{B}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Angel of Jubilation");
        harness.assertNotOnBattlefield(player2, "Angel of Jubilation");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Paying zero life to cast a spell remains legal")
    void canPayZeroLifeToCastSpell() {
        Permanent angel = addCreatureReady(player1, new AngelOfJubilation());
        harness.setHand(player1, List.of(new Hatred()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0, 0, angel.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(3);
        harness.assertInGraveyard(player1, "Hatred");
    }

    @Test
    @DisplayName("Paying positive life to cast a spell is prohibited")
    void cantPayPositiveLifeToCastSpell() {
        Permanent angel = addCreatureReady(player1, new AngelOfJubilation());
        harness.setHand(player1, List.of(new Hatred()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, angel.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("pay life");

        harness.assertLife(player1, 20);
        harness.assertInHand(player1, "Hatred");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Paying zero life to activate an ability remains legal")
    void canPayZeroLifeToActivateAbility() {
        addCreatureReady(player1, new AngelOfJubilation());
        Permanent initiate = addCreatureReady(player1, new KrumarInitiate());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 1, 0, null);

        assertThat(initiate.isTapped()).isTrue();
        harness.assertLife(player1, 20);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
    }
}
