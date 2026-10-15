package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DemonicEmbrace.class, GrizzlyBears.class, FountainOfYouth.class, Mountain.class, Unsummon.class})
class DemonicEmbraceTest extends BaseCardTest {

    @Test
    @DisplayName("Demonic Embrace boosts and changes the enchanted creature")
    void boostsAndChangesEnchantedCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DemonicEmbrace()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
        assertThat(gqs.effectiveCreatureSubtypes(gd, bears)).contains(CardSubtype.DEMON);
    }

    @Test
    @DisplayName("Demonic Embrace can target only a creature")
    void targetsOnlyCreature() {
        Permanent fountain = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new DemonicEmbrace()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, fountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Demonic Embrace can be cast from the graveyard by paying life and discarding")
    void castsFromGraveyardWithLifeAndDiscard() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new DemonicEmbrace()));
        harness.setHand(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castRetrace(player1, 0, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
        harness.assertInGraveyard(player1, "Mountain");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
        assertThat(gqs.effectiveCreatureSubtypes(gd, bears)).contains(CardSubtype.DEMON);
    }

    @Test
    @DisplayName("Demonic Embrace cannot be cast from the graveyard without a discard")
    void requiresDiscardToCastFromGraveyard() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new DemonicEmbrace()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castFromGraveyardTargeting(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must discard exactly 1");
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(3);
    }

    @Test
    void retainsOriginalSubtypeAndDoesNotAffectOtherCreatures() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DemonicEmbrace(), new Mountain()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castEnchantment(player1, 0, enchanted.getId());
        harness.passBothPriorities();

        assertThat(gqs.effectiveCreatureSubtypes(gd, enchanted)).contains(CardSubtype.BEAR, CardSubtype.DEMON);
        assertThat(gqs.effectiveCreatureSubtypes(gd, other)).doesNotContain(CardSubtype.DEMON);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, other, Keyword.FLYING)).isFalse();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        harness.assertInHand(player1, "Mountain");
    }

    @Test
    void graveyardCastRequiresNormalManaCost() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new DemonicEmbrace()));
        harness.setHand(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castRetrace(player1, 0, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        harness.assertInHand(player1, "Mountain");
        harness.assertInGraveyard(player1, "Demonic Embrace");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void graveyardCastRequiresEnoughLife() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new DemonicEmbrace()));
        harness.setHand(player1, List.of(new Mountain()));
        harness.setLife(player1, 2);
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castRetrace(player1, 0, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.getLife(player1.getId())).isEqualTo(2);
        harness.assertInHand(player1, "Mountain");
        harness.assertInGraveyard(player1, "Demonic Embrace");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void graveyardPermissionDoesNotAllowCastingDuringCombat() {
        harness.forceActivePlayer(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new DemonicEmbrace()));
        harness.setHand(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.castRetrace(player1, 0, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery-speed");

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        harness.assertInHand(player1, "Mountain");
        harness.assertInGraveyard(player1, "Demonic Embrace");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(3);
    }

    @Test
    void failedGraveyardAuraKeepsCostsPaidAndCanBeCastAgain() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        DemonicEmbrace embrace = new DemonicEmbrace();
        harness.setGraveyard(player1, List.of(embrace));
        harness.setHand(player1, List.of(new Mountain(), new Mountain()));
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castRetrace(player1, 0, 0, first.getId());
        harness.castInstant(player2, 0, first.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Demonic Embrace");
        harness.assertNotOnBattlefield(player1, "Demonic Embrace");
        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();

        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        int embraceIndex = gd.playerGraveyards.get(player1.getId()).indexOf(embrace);
        harness.castRetrace(player1, embraceIndex, 0, second.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Demonic Embrace");
        harness.assertNotInGraveyard(player1, "Demonic Embrace");
        assertThat(gd.getLife(player1.getId())).isEqualTo(14);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, second, Keyword.FLYING)).isTrue();
        assertThat(gqs.effectiveCreatureSubtypes(gd, second)).contains(CardSubtype.BEAR, CardSubtype.DEMON);
    }

    @Test
    void auraReturnsToGraveyardWhenEnchantedCreatureLeaves() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new DemonicEmbrace()));
        harness.setHand(player1, List.of(new Mountain(), new GrizzlyBears()));
        harness.setHand(player2, List.of(new Unsummon()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castRetrace(player1, 0, 1, bears.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Demonic Embrace");
        harness.assertInHand(player1, "Mountain");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.getLife(player1.getId())).isEqualTo(17);

        harness.castAndResolveInstant(player2, 0, bears.getId());

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Demonic Embrace");
        harness.assertInGraveyard(player1, "Demonic Embrace");
        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
    }
}
