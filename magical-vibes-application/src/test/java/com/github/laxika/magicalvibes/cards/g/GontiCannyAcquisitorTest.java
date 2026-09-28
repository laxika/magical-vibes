package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GontiCannyAcquisitor.class, Divination.class, GrizzlyBears.class, Island.class})
class GontiCannyAcquisitorTest extends BaseCardTest {

    @Test
    void reducesCostOfSpellsTheControllerDoesNotOwn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new GontiCannyAcquisitor());

        Divination spell = new Divination();
        spell.setOwnerId(player2.getId());
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .contains(spell.getId());
    }

    @Test
    void doesNotReduceCostOfSpellsTheControllerOwns() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new GontiCannyAcquisitor());

        Divination spell = new Divination();
        spell.setOwnerId(player1.getId());
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void combatDamageExilesOnlyOneCardFaceDownWithPersistentAnyManaPermission() {
        Permanent gonti = harness.addToBattlefieldAndReturn(player1, new GontiCannyAcquisitor());
        addAttacker(new GrizzlyBears());
        addAttacker(new GrizzlyBears());
        Card topCard = new Divination();
        harness.setLibrary(player2, List.of(topCard, new Island()));

        resolveCombat();
        harness.passBothPriorities();

        ExiledCardEntry entry = gd.findExiledCard(topCard.getId());
        assertThat(entry).isNotNull();
        assertThat(entry.faceDown()).isTrue();
        assertThat(entry.sourcePermanentId()).isEqualTo(gonti.getId());
        assertThat(entry.exilerId()).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
        assertThat(gd.exilePlayAnyManaTypeWhileExiled).contains(topCard.getId());
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(1);
    }

    private void addAttacker(Card card) {
        Permanent attacker = addCreatureReady(player1, card);
        attacker.setAttacking(true);
    }
}
