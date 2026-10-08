package com.github.laxika.magicalvibes.cards.x;

import com.github.laxika.magicalvibes.cards.f.FireMagic;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.MagitekInfantry;
import com.github.laxika.magicalvibes.cards.w.WhiteMagesStaff;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({XandeDarkMage.class, FireMagic.class, Forest.class, MagitekInfantry.class, WhiteMagesStaff.class})
class XandeDarkMageTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 for each noncreature, nonland card in its controller's graveyard")
    void getsBoostFromQualifyingGraveyardCards() {
        Permanent xande = addCreatureReady(player1, new XandeDarkMage());
        harness.setGraveyard(player1, List.of(new FireMagic(), new FireMagic(), new Forest(), new MagitekInfantry()));

        assertThat(gqs.getEffectivePower(gd, xande)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, xande)).isEqualTo(5);
    }

    @Test
    @DisplayName("Counts only its controller's qualifying graveyard cards")
    void ignoresOtherCardTypesAndOpponentsGraveyard() {
        Permanent xande = addCreatureReady(player1, new XandeDarkMage());
        harness.setGraveyard(player1, List.of(new Forest(), new MagitekInfantry(), new FireMagic()));
        harness.setGraveyard(player2, List.of(new FireMagic(), new FireMagic()));

        assertThat(gqs.getEffectivePower(gd, xande)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, xande)).isEqualTo(4);
    }

    @Test
    @DisplayName("Updates as qualifying cards enter or leave the graveyard")
    void updatesDynamically() {
        Permanent xande = addCreatureReady(player1, new XandeDarkMage());
        harness.setGraveyard(player1, List.of(new FireMagic()));

        assertThat(gqs.getEffectivePower(gd, xande)).isEqualTo(4);

        harness.setGraveyard(player1, List.of(new FireMagic(), new FireMagic(), new Forest(), new MagitekInfantry()));

        assertThat(gqs.getEffectivePower(gd, xande)).isEqualTo(5);

        harness.setGraveyard(player1, List.of(new FireMagic(), new Forest(), new MagitekInfantry()));

        assertThat(gqs.getEffectivePower(gd, xande)).isEqualTo(4);
    }

    @Test
    void noBonusWithEmptyGraveyard() {
        Permanent xande = addCreatureReady(player1, new XandeDarkMage());
        harness.setGraveyard(player1, List.of());

        assertThat(gqs.getEffectivePower(gd, xande)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, xande)).isEqualTo(3);
    }

    @Test
    void countsNoncreatureArtifactsButNotArtifactCreatures() {
        Permanent xande = addCreatureReady(player1, new XandeDarkMage());
        harness.setGraveyard(player1, List.of(
                new WhiteMagesStaff(), new MagitekInfantry(), new FireMagic()));

        assertThat(gqs.getEffectivePower(gd, xande)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, xande)).isEqualTo(5);
    }

    @Test
    void menaceRejectsOneBlocker() {
        addCreatureReady(player1, new XandeDarkMage());
        addCreatureReady(player2, new MagitekInfantry());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
    }

    @Test
    void menaceAllowsTwoBlockers() {
        addCreatureReady(player1, new XandeDarkMage());
        Permanent first = addCreatureReady(player2, new MagitekInfantry());
        Permanent second = addCreatureReady(player2, new MagitekInfantry());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(first.isBlocking()).isTrue();
        assertThat(second.isBlocking()).isTrue();
    }
}
