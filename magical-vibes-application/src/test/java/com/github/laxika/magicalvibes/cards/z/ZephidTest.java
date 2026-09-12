package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.cards.p.PathOfPeace;
import com.github.laxika.magicalvibes.cards.w.WizardMentor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Zephid.class, PathOfPeace.class, WizardMentor.class, CoralMerfolk.class})
class ZephidTest extends BaseCardTest {

    @Test
    @DisplayName("Zephid cannot be targeted by spells because it has shroud")
    void cannotBeTargetedBySpells() {
        harness.addToBattlefield(player1, new Zephid());
        Permanent zephid = findPermanent(player1, "Zephid");

        harness.setHand(player1, List.of(new PathOfPeace()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, zephid.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("Zephid cannot be targeted by activated abilities because it has shroud")
    void cannotBeTargetedByAbilities() {
        addCreatureReady(player1, new WizardMentor());
        Permanent zephid = addCreatureReady(player1, new Zephid());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, zephid.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("Flying prevents a non-flying creature from blocking Zephid")
    void flyingPreventsNonFlyingCreatureFromBlocking() {
        addCreatureReady(player1, new Zephid());
        addCreatureReady(player2, new CoralMerfolk());

        declareAttackers(player1, List.of(0));
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(
                        gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot block");
    }
}
