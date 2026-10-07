package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HardenedAcademic;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SuspendAggression.class, HardenedAcademic.class, Island.class, Forest.class})
class SuspendAggressionTest extends BaseCardTest {

    private void addMana(com.github.laxika.magicalvibes.model.Player player) {
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.COLORLESS, 1);
    }

    private Card setTopCard(com.github.laxika.magicalvibes.model.Player player) {
        Card top = new Island();
        harness.setLibrary(player, List.of(top, new Island(), new Island()));
        return top;
    }

    @Test
    @DisplayName("Exiles the target permanent and grants its owner play permission")
    void exilesTargetPermanentForItsOwner() {
        Permanent academic = harness.addToBattlefieldAndReturn(player2, new HardenedAcademic());
        UUID academicCardId = academic.getOriginalCard().getId();
        Card top = setTopCard(player1);

        harness.setHand(player1, List.of(new SuspendAggression()));
        addMana(player1);
        harness.castInstant(player1, 0, academic.getId());
        harness.passBothPriorities();

        // Target permanent exiled; opponent (its owner) may play it.
        harness.assertNotOnBattlefield(player2, "Hardened Academic");
        assertThat(gd.exilePlayPermissions.get(academicCardId)).isEqualTo(player2.getId());

        // Top card of caster's library exiled; caster may play it.
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);
        assertThat(gd.exilePlayPermissions.get(top.getId())).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Opponent's card expires at the end of the opponent's upcoming turn")
    void opponentPermissionOwnerRelativeExpiry() {
        Permanent academic = harness.addToBattlefieldAndReturn(player2, new HardenedAcademic());
        UUID academicCardId = academic.getOriginalCard().getId();
        Card top = setTopCard(player1);

        harness.setHand(player1, List.of(new SuspendAggression()));
        addMana(player1);
        harness.castInstant(player1, 0, academic.getId());
        harness.passBothPriorities();

        harness.passUntilWithNoAttackers(player2, TurnStep.POSTCOMBAT_MAIN);
        assertThat(gd.exilePlayPermissions).containsKeys(academicCardId, top.getId());

        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(academicCardId).containsKey(top.getId());

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.exilePlayPermissions).doesNotContainKeys(academicCardId, top.getId());
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getId).contains(academicCardId);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);
    }

    @Test
    @DisplayName("Can target your own permanent, granting yourself play permission")
    void canTargetOwnPermanent() {
        Permanent academic = harness.addToBattlefieldAndReturn(player1, new HardenedAcademic());
        UUID academicCardId = academic.getOriginalCard().getId();
        setTopCard(player1);

        harness.setHand(player1, List.of(new SuspendAggression()));
        addMana(player1);
        harness.castInstant(player1, 0, academic.getId());
        harness.passBothPriorities();

        assertThat(gd.exilePlayPermissions.get(academicCardId)).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addToBattlefield(player1, new HardenedAcademic()); // a valid target so the spell is playable
        setTopCard(player1);

        harness.setHand(player1, List.of(new SuspendAggression()));
        addMana(player1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a nonland permanent");
    }

    @Test
    @DisplayName("An illegal sole target prevents the library card from being exiled")
    void illegalTargetStopsEntireSpell() {
        Permanent academic = harness.addToBattlefieldAndReturn(player2, new HardenedAcademic());
        Card top = setTopCard(player1);
        harness.setHand(player1, List.of(new SuspendAggression()));
        addMana(player1);
        harness.castInstant(player1, 0, academic.getId());
        gd.playerBattlefields.get(player2.getId()).remove(academic);
        gd.playerGraveyards.get(player2.getId()).add(academic.getOriginalCard());

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(top);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(top);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(top.getId());
    }

    @Test
    @DisplayName("An empty library does not prevent exiling the permanent")
    void emptyLibraryStillExilesPermanent() {
        Permanent academic = harness.addToBattlefieldAndReturn(player2, new HardenedAcademic());
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new SuspendAggression()));
        addMana(player1);
        harness.castInstant(player1, 0, academic.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Hardened Academic");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(academic.getOriginalCard());
        assertThat(gd.exilePlayPermissions.get(academic.getOriginalCard().getId())).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("The library's exiled land can be played but still uses the normal land allowance")
    void playsExiledLandWithNormalLimit() {
        Permanent academic = harness.addToBattlefieldAndReturn(player2, new HardenedAcademic());
        Card top = setTopCard(player1);
        harness.setHand(player1, List.of(new SuspendAggression(), new Forest()));
        addMana(player1);
        harness.castInstant(player1, 0, academic.getId());
        harness.passBothPriorities();

        harness.castFromExile(player1, top.getId());

        harness.assertOnBattlefield(player1, "Island");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(top);
        assertThatThrownBy(() -> harness.playLand(player1, 0)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The owner can recast the exiled creature by paying its normal mana cost")
    void recastsExiledCreature() {
        Permanent academic = harness.addToBattlefieldAndReturn(player2, new HardenedAcademic());
        setTopCard(player1);
        harness.setHand(player1, List.of(new SuspendAggression()));
        addMana(player1);
        harness.castInstant(player1, 0, academic.getId());
        harness.passBothPriorities();
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castFromExile(player2, academic.getOriginalCard().getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.castFromExile(player2, academic.getOriginalCard().getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Hardened Academic");
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(academic.getOriginalCard());
    }

    @Test
    @DisplayName("A stolen permanent grants permission to its owner rather than its controller")
    void stolenPermanentPermissionBelongsToOwner() {
        HardenedAcademic card = new HardenedAcademic();
        card.setOwnerId(player2.getId());
        Permanent academic = harness.addToBattlefieldAndReturn(player1, card);
        setTopCard(player1);
        harness.setHand(player1, List.of(new SuspendAggression()));
        addMana(player1);
        harness.castInstant(player1, 0, academic.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Hardened Academic");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(card);
        assertThat(gd.exilePlayPermissions.get(card.getId())).isEqualTo(player2.getId());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        assertThatThrownBy(() -> harness.castFromExile(player1, card.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
