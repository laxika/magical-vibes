package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SummoningMateria.class, Forest.class, GrizzlyBears.class})
class SummoningMateriaTest extends BaseCardTest {

    @Test
    void attachedCreatureGetsBoostVigilanceAndGreenManaAbility() {
        Permanent materia = addReadyMateria(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        materia.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();

        harness.activateAbility(player1, 1, null, null);

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    void attachedMateriaCastsCreatureFromLibraryTop() {
        Permanent materia = addReadyMateria(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        materia.setAttachedTo(creature.getId());
        GrizzlyBears topCreature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCreature));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFromLibraryTop(player1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(topCreature);
    }

    @Test
    void unattachedMateriaCannotCastFromLibraryTop() {
        addReadyMateria(player1);
        GrizzlyBears topCreature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCreature));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topCreature);
    }

    @Test
    void materiaCannotCastNoncreatureFromLibraryTop() {
        Permanent materia = addReadyMateria(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        materia.setAttachedTo(creature.getId());
        Forest topLand = new Forest();
        harness.setLibrary(player1, List.of(topLand));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topLand);
    }

    private Permanent addReadyMateria(Player player) {
        Permanent materia = new Permanent(new SummoningMateria());
        materia.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(materia);
        return materia;
    }
}
