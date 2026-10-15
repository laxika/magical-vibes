package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.b.BronzeSword;
import com.github.laxika.magicalvibes.cards.n.NyxbornCourser;
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

@CardUsed({IrreverentRevelers.class, BronzeSword.class, NyxbornCourser.class})
class IrreverentRevelersTest extends BaseCardTest {

    @Test
    @DisplayName("ETB mode destroys target artifact")
    void destroysArtifactMode() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new BronzeSword());

        castRevelers(0, artifact.getId());
        resolveCreatureAndEtb();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(artifact);
    }

    @Test
    @DisplayName("Destroy artifact mode rejects a creature target")
    void destroyModeRejectsCreatureTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new NyxbornCourser());
        harness.addToBattlefield(player2, new BronzeSword());
        harness.enterBattlefieldAndReturn(player1, new IrreverentRevelers());
        harness.handleListChoice(player1, "Destroy target artifact");

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("ETB mode gives Irreverent Revelers haste until end of turn")
    void hasteMode() {
        castRevelers(1, null);
        resolveCreatureAndEtb();

        Permanent revelers = findPermanent(player1, "Irreverent Revelers");
        assertThat(gqs.hasKeyword(gd, revelers, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, revelers, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Destroy mode can target an artifact you control")
    void destroysOwnArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new BronzeSword());

        castRevelers(0, artifact.getId());
        resolveCreatureAndEtb();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(artifact);
        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Irreverent Revelers"), Keyword.HASTE))
                .isFalse();
    }

    @Test
    @DisplayName("Haste mode leaves artifacts intact and grants haste only to its source")
    void hasteModeWithArtifactPresent() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new BronzeSword());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new NyxbornCourser());

        castRevelers(1, null);
        resolveCreatureAndEtb();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(artifact);
        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Irreverent Revelers"), Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Entering without being cast still allows choosing haste")
    void enteringWithoutCastingOffersHasteMode() {
        harness.addToBattlefield(player2, new BronzeSword());
        Permanent revelers = harness.enterBattlefieldAndReturn(player1, new IrreverentRevelers());

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleListChoice(player1, "Irreverent Revelers gains haste until end of turn");
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, revelers, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Casting needs no artifact target; the mode is chosen after the creature resolves")
    void choosesModeAfterCreatureResolves() {
        harness.addToBattlefield(player2, new BronzeSword());
        prepareRevelers();

        harness.castCreature(player1, 0);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleListChoice(player1, "Irreverent Revelers gains haste until end of turn");
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Irreverent Revelers"), Keyword.HASTE)).isTrue();
    }

    private void castRevelers(int mode, java.util.UUID targetId) {
        prepareRevelers();
        if (targetId == null) {
            harness.castCreature(player1, 0, mode);
        } else {
            harness.castCreature(player1, 0, mode, targetId);
        }
    }

    private void prepareRevelers() {
        harness.setHand(player1, List.of(new IrreverentRevelers()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private void resolveCreatureAndEtb() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
