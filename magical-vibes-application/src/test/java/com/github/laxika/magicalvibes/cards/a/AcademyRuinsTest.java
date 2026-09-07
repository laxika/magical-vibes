package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AcademyRuins.class, Ornithopter.class, GrizzlyBears.class})
class AcademyRuinsTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping adds one colorless mana")
    void tapsForColorless() {
        Permanent ruins = addReadyRuins();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(ruins.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Puts a target artifact card from the graveyard on top of the library")
    void putsTargetArtifactOnTopOfLibrary() {
        Permanent ruins = addReadyRuins();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        Card artifact = new Ornithopter();
        harness.setGraveyard(player1, List.of(artifact));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        int ruinsIndex = gd.playerBattlefields.get(player1.getId()).indexOf(ruins);
        harness.activateAbility(player1, ruinsIndex, 1, null, artifact.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(artifact);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(artifact);
    }

    @Test
    @DisplayName("Only artifact cards in your graveyard are legal targets")
    void rejectsNonArtifactTarget() {
        Permanent ruins = addReadyRuins();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        Card nonArtifact = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(nonArtifact));

        int ruinsIndex = gd.playerBattlefields.get(player1.getId()).indexOf(ruins);
        assertThatThrownBy(() -> harness.activateAbility(
                player1, ruinsIndex, 1, null, nonArtifact.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyRuins() {
        Permanent ruins = harness.addToBattlefieldAndReturn(player1, new AcademyRuins());
        ruins.setSummoningSick(false);
        return ruins;
    }
}
