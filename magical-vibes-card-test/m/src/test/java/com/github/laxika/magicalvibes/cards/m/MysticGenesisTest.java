package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.e.EsixFractalBloom;
import com.github.laxika.magicalvibes.cards.i.ImmortalServitude;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.SupremeVerdict;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MysticGenesis.class, GrizzlyBears.class, SerraAngel.class,
        ImmortalServitude.class, SupremeVerdict.class, EsixFractalBloom.class})
class MysticGenesisTest extends BaseCardTest {

    private void prepare() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new MysticGenesis()));
        harness.addMana(player2, ManaColor.BLUE, 4); // {2}{G}{U}{U}
        harness.addMana(player2, ManaColor.GREEN, 1);
    }

    private List<Permanent> tokensOf(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .toList();
    }

    @Test
    @DisplayName("Counters the spell and creates an Ooze token sized to that spell's mana value")
    void countersAndCreatesOozeSizedToManaValue() {
        prepare();
        GrizzlyBears bears = new GrizzlyBears(); // mana value 2
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");

        List<Permanent> tokens = tokensOf(player2);
        assertThat(tokens).hasSize(1);
        Permanent ooze = tokens.getFirst();
        assertThat(ooze.getCard().getName()).isEqualTo("Ooze");
        assertThat(ooze.getCard().getPower()).isEqualTo(2);
        assertThat(ooze.getCard().getToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Token size follows the countered spell's mana value")
    void tokenSizeFollowsCounteredSpell() {
        prepare();
        SerraAngel angel = new SerraAngel(); // mana value 5
        harness.setHand(player1, List.of(angel));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, angel.getId());

        harness.assertInGraveyard(player1, "Serra Angel");

        List<Permanent> tokens = tokensOf(player2);
        assertThat(tokens).hasSize(1);
        assertThat(tokens.getFirst().getCard().getPower()).isEqualTo(5);
        assertThat(tokens.getFirst().getCard().getToughness()).isEqualTo(5);
    }

    @Test
    void includesChosenXInSpellManaValue() {
        prepare();
        ImmortalServitude servitude = new ImmortalServitude();
        harness.setHand(player1, List.of(servitude));
        harness.addMana(player1, ManaColor.WHITE, 7);

        harness.castSorcery(player1, 0, 4);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, servitude.getId());

        harness.assertInGraveyard(player1, "Immortal Servitude");
        assertThat(tokensOf(player2)).singleElement().satisfies(token -> {
            assertThat(token.getCard().getPower()).isEqualTo(7);
            assertThat(token.getCard().getToughness()).isEqualTo(7);
        });
    }

    @Test
    void createsTokenEvenWhenSpellCannotBeCountered() {
        prepare();
        SupremeVerdict verdict = new SupremeVerdict();
        harness.setHand(player1, List.of(verdict));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, verdict.getId());

        harness.assertNotInGraveyard(player1, "Supreme Verdict");
        assertThat(gd.stack).hasSize(1);
        assertThat(tokensOf(player2)).singleElement().satisfies(token -> {
            assertThat(token.getCard().getPower()).isEqualTo(4);
            assertThat(token.getCard().getToughness()).isEqualTo(4);
        });

        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Supreme Verdict");
        assertThat(tokensOf(player2)).isEmpty();
    }

    @Test
    void createsNoTokenWhenTargetHasLeftStack() {
        prepare();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player2, List.of(new MysticGenesis(), new MysticGenesis()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.castAndResolveInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
        assertThat(tokensOf(player2)).hasSize(1);
    }

    @Test
    void countersSpellBeforeOfferingTokenReplacement() {
        prepare();
        harness.forceActivePlayer(player2);
        harness.addToBattlefield(player2, new EsixFractalBloom());
        harness.addToBattlefield(player2, new SerraAngel());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player2, List.of(new MysticGenesis(), bears));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player2, 1);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.handleMayAbilityChosen(player2, false);
        assertThat(tokensOf(player2)).singleElement().satisfies(token -> {
            assertThat(token.getCard().getPower()).isEqualTo(2);
            assertThat(token.getCard().getToughness()).isEqualTo(2);
        });
    }
}
