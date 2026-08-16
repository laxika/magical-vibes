package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CopperMyr;
import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DrafnaFounderOfLatNamTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a target artifact you control to its owner's hand")
    void returnsTargetArtifactToHand() {
        addReadyDrafna();
        Permanent copperMyr = addCreatureReady(player1, new CopperMyr());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, copperMyr.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Copper Myr");
        harness.assertNotOnBattlefield(player1, "Copper Myr");
    }

    @Test
    @DisplayName("Copies an artifact spell as a token without granting haste")
    void copiesArtifactSpellAsToken() {
        addReadyDrafna();
        CopperMyr copperMyr = new CopperMyr();
        harness.setHand(player1, List.of(copperMyr));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castArtifact(player1, 0);
        harness.activateAbility(player1, 0, 1, null, copperMyr.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).filteredOn(StackEntry::isCopy).hasSize(1);
        assertThat(gd.pendingMayAbilities).isEmpty();

        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> copperMyrs = findPermanents(player1, "Copper Myr");
        assertThat(copperMyrs).hasSize(2);
        Permanent copy = copperMyrs.stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(copy.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Cannot return an artifact controlled by an opponent")
    void cannotReturnOpponentArtifact() {
        addReadyDrafna();
        Permanent opponentArtifact = addCreatureReady(player2, new CopperMyr());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, opponentArtifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot copy a nonartifact spell")
    void cannotCopyNonartifactSpell() {
        addReadyDrafna();
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castSorcery(player1, 0, 0);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, counsel.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addReadyDrafna() {
        addCreatureReady(player1, new DrafnaFounderOfLatNam());
    }
}
