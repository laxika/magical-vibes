package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KamberThePlunderer;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LaurineTheDiversion.class, KamberThePlunderer.class, Forest.class, GrizzlyBears.class, Spellbook.class})
class LaurineTheDiversionTest extends BaseCardTest {

    @Test
    @DisplayName("Partner with lets the target player search for Kamber")
    void partnerWithSearchesForKamber() {
        Card decoy = new Forest();
        Card partner = new KamberThePlunderer();
        harness.setLibrary(player2, List.of(decoy, partner));

        harness.enterBattlefieldAndReturn(player1, new LaurineTheDiversion());
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.validPlayerIds()).contains(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getName)
                .contains("Kamber, the Plunderer");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(decoy);
    }

    @Test
    @DisplayName("Sacrificing an artifact goads the target creature")
    void sacrificesArtifactAndGoadsTarget() {
        addReadyLaurine();
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(artifact);
        assertThat(als.getMustAttackRequirementCount(gd, target)).isEqualTo(1);
    }

    @Test
    @DisplayName("Laurine herself may be sacrificed as the creature cost")
    void sourceCanBeSacrificed() {
        Permanent laurine = addReadyLaurine();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(laurine);
        assertThat(als.getMustAttackRequirementCount(gd, target)).isEqualTo(1);
    }

    @Test
    @DisplayName("The activated ability cannot target a noncreature permanent")
    void cannotTargetNonCreaturePermanent() {
        addReadyLaurine();
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Spellbook());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyLaurine() {
        return addCreatureReady(player1, new LaurineTheDiversion());
    }
}
