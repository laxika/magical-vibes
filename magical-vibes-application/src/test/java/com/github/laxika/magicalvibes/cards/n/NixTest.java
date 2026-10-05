package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.Aluren;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HogaakArisenNecropolis;
import com.github.laxika.magicalvibes.cards.s.SummonersPact;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Nix.class, Aluren.class, GrizzlyBears.class, SummonersPact.class, HogaakArisenNecropolis.class})
class NixTest extends BaseCardTest {

    @Test
    void countersSpellCastWithoutSpendingMana() {
        harness.addToBattlefield(player1, new Aluren());
        GrizzlyBears targetSpell = new GrizzlyBears();
        harness.setHand(player1, List.of(targetSpell));
        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new Nix()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, targetSpell.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    void doesNotCounterSpellCastWithMana() {
        GrizzlyBears targetSpell = new GrizzlyBears();
        harness.setHand(player1, List.of(targetSpell));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new Nix()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, targetSpell.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Nix");
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    void countersZeroCostSpellEvenWithManaAvailable() {
        SummonersPact targetSpell = new SummonersPact();
        harness.setHand(player1, List.of(targetSpell));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castInstant(player1, 0);
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new Nix()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, targetSpell.getId());

        harness.assertInGraveyard(player1, "Summoner's Pact");
        harness.assertInGraveyard(player2, "Nix");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void countersOwnZeroCostSpell() {
        SummonersPact targetSpell = new SummonersPact();
        harness.setHand(player1, List.of(targetSpell, new Nix()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0);
        harness.castAndResolveInstant(player1, 0, targetSpell.getId());

        harness.assertInGraveyard(player1, "Summoner's Pact");
        harness.assertInGraveyard(player1, "Nix");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void countersHogaakCastFromGraveyardUsingOnlyConvoke() {
        HogaakArisenNecropolis targetSpell = new HogaakArisenNecropolis();
        harness.setGraveyard(player1, List.of(targetSpell));
        List<Permanent> creatures = IntStream.range(0, 7)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()))
                .toList();

        gs.playFlashbackSpell(gd, player1, 0, null, null, List.of(), List.of(),
                null, List.of(), null, null, List.of(), Map.of(), List.of(), List.of(), List.of(),
                creatures.stream().map(Permanent::getId).toList());
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new Nix()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, targetSpell.getId());

        assertThat(creatures).allMatch(Permanent::isTapped);
        harness.assertInGraveyard(player1, "Hogaak, Arisen Necropolis");
        harness.assertInGraveyard(player2, "Nix");
        assertThat(gd.stack).isEmpty();
    }
}
