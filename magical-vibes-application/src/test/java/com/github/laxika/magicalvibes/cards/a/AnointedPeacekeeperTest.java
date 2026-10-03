package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.b.BirdsOfParadise;
import com.github.laxika.magicalvibes.cards.c.CultConscript;
import com.github.laxika.magicalvibes.cards.f.FaerieMacabre;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.s.SheoldredsRestoration;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AnointedPeacekeeper.class, GrizzlyBears.class, Shock.class, BirdsOfParadise.class,
        ProdigalPyromancer.class, CultConscript.class, FaerieMacabre.class, SheoldredsRestoration.class})
class AnointedPeacekeeperTest extends BaseCardTest {

    @Test
    void looksAtOpponentHandAndChoosesCardName() {
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new AnointedPeacekeeper()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(entry -> entry.plainText()))
                .anyMatch(log -> log.contains("looks at") && log.contains("hand"));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "Grizzly Bears");

        assertThat(findPermanent(player1, "Anointed Peacekeeper").getChosenName())
                .isEqualTo("Grizzly Bears");
    }

    @Test
    void taxesOpponentsCastingChosenName() {
        addReadyPeacekeeper(player1, "Grizzly Bears");
        preparePlayer2MainPhase();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");

        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castCreature(player2, 0);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void doesNotTaxOtherNamesOrControllerSpells() {
        addReadyPeacekeeper(player1, "Grizzly Bears");

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        preparePlayer2MainPhase();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void taxesNamedNonManaAbilities() {
        addReadyPeacekeeper(player1, "Prodigal Pyromancer");
        addPermanent(player2, new ProdigalPyromancer());
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.activateAbility(player2, 0, null, player1.getId());
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void doesNotTaxNamedManaAbilities() {
        addReadyPeacekeeper(player1, "Birds of Paradise");
        addPermanent(player2, new BirdsOfParadise());

        harness.activateAbility(player2, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
    }

    @Test
    void canChooseNameAbsentFromEmptyOpponentHand() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new AnointedPeacekeeper()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Shock");

        assertThat(findPermanent(player1, "Anointed Peacekeeper").getChosenName()).isEqualTo("Shock");
    }

    @Test
    void taxesControllersOwnNamedNonManaAbility() {
        addReadyPeacekeeper(player1, "Prodigal Pyromancer");
        addPermanent(player1, new ProdigalPyromancer());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();
        harness.assertLife(player2, 19);
    }

    @Test
    void multiplePeacekeepersStackSpellTaxes() {
        addReadyPeacekeeper(player1, "Grizzly Bears");
        addReadyPeacekeeper(player1, "Grizzly Bears");
        preparePlayer2MainPhase();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("not playable");

        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void removingPeacekeeperEndsSpellTax() {
        Permanent peacekeeper = addReadyPeacekeeper(player1, "Grizzly Bears");
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, peacekeeper.getId());
        harness.castAndResolveInstant(player1, 0, peacekeeper.getId());
        harness.assertInGraveyard(player1, "Anointed Peacekeeper");

        preparePlayer2MainPhase();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void taxesNamedGraveyardAbility() {
        addReadyPeacekeeper(player1, "Cult Conscript");
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player2, List.of(new CultConscript()));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, bears.getId());
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player2, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.activateGraveyardAbility(player2, 0);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Cult Conscript");
    }

    @Test
    void taxesHandAbilityWithoutPrintedManaCost() {
        addReadyPeacekeeper(player1, "Faerie Macabre");
        harness.setHand(player2, List.of(new FaerieMacabre()));
        GrizzlyBears target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));

        assertThatThrownBy(() -> harness.activateHandAbilityWithGraveyardTargets(
                player2, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.activateHandAbilityWithGraveyardTargets(player2, 0, List.of(target.getId()));
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(target);
        harness.assertInGraveyard(player2, "Faerie Macabre");
    }

    @Test
    void reanimationStillLooksAtHandAndChoosesName() {
        AnointedPeacekeeper peacekeeper = new AnointedPeacekeeper();
        harness.setGraveyard(player1, List.of(peacekeeper));
        harness.setHand(player1, List.of(new SheoldredsRestoration()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, peacekeeper.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        assertThat(gd.gameLog.stream().map(entry -> entry.plainText()))
                .anyMatch(log -> log.contains("looks at") && log.contains("hand"));
        harness.handleListChoice(player1, "Grizzly Bears");
        assertThat(findPermanent(player1, "Anointed Peacekeeper").getChosenName())
                .isEqualTo("Grizzly Bears");
    }

    private Permanent addReadyPeacekeeper(Player player, String chosenName) {
        Permanent permanent = addPermanent(player, new AnointedPeacekeeper());
        permanent.setChosenName(chosenName);
        return permanent;
    }

    private Permanent addPermanent(Player player, Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }

    private void preparePlayer2MainPhase() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

}
