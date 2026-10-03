package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.cards.v.VaultSkirge;
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

@CardUsed({WireSurgeons.class, VaultSkirge.class, GrizzlyBears.class, SolRing.class})
class WireSurgeonsTest extends BaseCardTest {

    @Test
    @DisplayName("Artifact creature cards in your graveyard gain encore for their mana value")
    void grantsEncoreToArtifactCreaturesInGraveyard() {
        harness.addToBattlefield(player1, new WireSurgeons());
        harness.setGraveyard(player1, List.of(new VaultSkirge()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent token = findPermanents(player1, "Vault Skirge").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.isTapped()).isTrue();
        assertThat(token.isAttacking()).isTrue();
        assertThat(token.getAttackTarget()).isEqualTo(player2.getId());
        assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Encore is not granted to non-artifact creature cards")
    void doesNotGrantEncoreToNonArtifactCreatures() {
        harness.addToBattlefield(player1, new WireSurgeons());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Encore is not granted to noncreature artifact cards")
    void doesNotGrantEncoreToNoncreatureArtifacts() {
        harness.addToBattlefield(player1, new WireSurgeons());
        harness.setGraveyard(player1, List.of(new SolRing()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
