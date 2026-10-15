package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KingMacarTheGoldCursed.class, GrizzlyBears.class, Forest.class})
class KingMacarTheGoldCursedTest extends BaseCardTest {

    @Test
    @DisplayName("Untapping King Macar may exile a target creature and create a Gold token")
    void untappingExilesCreatureAndCreatesGoldToken() {
        addTappedKingMacar();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToKingMacarTrigger();

        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(findPermanents(player1, "Gold")).hasSize(1);
        Permanent gold = findPermanent(player1, "Gold");
        assertThat(gold.getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(gold.getCard().getActivatedAbilities()).hasSize(1);
    }

    @Test
    @DisplayName("Declining King Macar's inspired ability leaves the target and creates no Gold")
    void decliningInspiredAbilityDoesNothing() {
        addTappedKingMacar();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToKingMacarTrigger();

        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(findPermanents(player1, "Gold")).isEmpty();
    }

    @Test
    @DisplayName("King Macar can target creatures but not lands")
    void targetChoiceOnlyOffersCreatures() {
        addTappedKingMacar();
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToKingMacarTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validPermanentIds()).contains(bears.getId()).doesNotContain(forest.getId());
    }

    private Permanent addTappedKingMacar() {
        Permanent kingMacar = harness.addToBattlefieldAndReturn(player1, new KingMacarTheGoldCursed());
        kingMacar.setSummoningSick(false);
        kingMacar.tap();
        return kingMacar;
    }

    private void advanceToKingMacarTrigger() {
        advanceToUpkeep(player1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
    }

    @Test
    void canExileItselfAndStillCreateGoldWithGoldSubtype() {
        Permanent kingMacar = addTappedKingMacar();
        advanceToKingMacarTrigger();
        harness.handlePermanentChosen(player1, kingMacar.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "King Macar, the Gold-Cursed");
        assertThat(gd.exiledCards).anySatisfy(entry -> {
            assertThat(entry.card()).isSameAs(kingMacar.getCard());
            assertThat(entry.ownerId()).isEqualTo(player1.getId());
        });
        assertThat(findPermanents(player1, "Gold")).hasSize(1);
        assertThat(findPermanent(player1, "Gold").getCard().getSubtypes()).contains(CardSubtype.GOLD);
        harness.assertNotOnBattlefield(player2, "Gold");
    }

    @Test
    void noGoldWhenTargetLeavesBeforeResolution() {
        Permanent kingMacar = addTappedKingMacar();
        advanceToKingMacarTrigger();
        harness.handlePermanentChosen(player1, kingMacar.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToExile(gd, kingMacar));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Gold")).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void tappedGoldCanBeSacrificedImmediatelyForMana() {
        Permanent kingMacar = addTappedKingMacar();
        advanceToKingMacarTrigger();
        harness.handlePermanentChosen(player1, kingMacar.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        findPermanent(player1, "Gold").tap();
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(findPermanents(player1, "Gold")).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void alreadyUntappedKingMacarDoesNotTrigger() {
        harness.addToBattlefield(player1, new KingMacarTheGoldCursed());
        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(findPermanents(player1, "Gold")).isEmpty();
    }
}
