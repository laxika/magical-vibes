package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.f.FireDiamond;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({JooDeeOneOfMany.class, GrizzlyBears.class, FireDiamond.class})
class JooDeeOneOfManyTest extends BaseCardTest {

    @Test
    @DisplayName("Surveils, creates a copy, and sacrifices a creature")
    void surveilsCreatesCopyAndSacrificesCreature() {
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        Permanent jooDee = addCreatureReady(player1, new JooDeeOneOfMany());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        activate(jooDee);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard, bears.getOriginalCard());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(jooDee)
                .anyMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Joo Dee, One of Many"));
    }

    @Test
    @DisplayName("Can sacrifice an artifact after creating the token copy")
    void canSacrificeArtifact() {
        Permanent jooDee = addCreatureReady(player1, new JooDeeOneOfMany());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FireDiamond());

        activate(jooDee);
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMultiplePermanentsChosen(player1, List.of(artifact.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(jooDee)
                .noneMatch(permanent -> permanent.getId().equals(artifact.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(artifact.getOriginalCard());
    }

    @Test
    @DisplayName("Can sacrifice Joo Dee itself")
    void canSacrificeSourceItself() {
        Permanent jooDee = addCreatureReady(player1, new JooDeeOneOfMany());

        activate(jooDee);
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMultiplePermanentsChosen(player1, List.of(jooDee.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(jooDee.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Joo Dee, One of Many"));
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(jooDee.getOriginalCard());
    }

    @Test
    @DisplayName("Can only be activated at sorcery speed")
    void canOnlyBeActivatedAtSorcerySpeed() {
        Permanent jooDee = addCreatureReady(player1, new JooDeeOneOfMany());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLACK, 1);

        int permanentIndex = gd.playerBattlefields.get(player1.getId()).indexOf(jooDee);
        assertThatThrownBy(() -> harness.activateAbility(player1, permanentIndex, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("main phase");
    }

    private void activate(Permanent jooDee) {
        harness.addMana(player1, ManaColor.BLACK, 1);
        int permanentIndex = gd.playerBattlefields.get(player1.getId()).indexOf(jooDee);
        harness.activateAbility(player1, permanentIndex, null, null);
        harness.passBothPriorities();
    }
}
