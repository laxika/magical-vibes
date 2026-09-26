package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AnointedPeacekeeper.class, GrizzlyBears.class, Shock.class})
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
        addPermanent(player2, createCreatureWithTapAbility("Prodigal Pyromancer"));
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.activateAbility(player2, 0, null, player1.getId());
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void doesNotTaxNamedManaAbilities() {
        addReadyPeacekeeper(player1, "Birds of Paradise");
        addPermanent(player2, createCreatureWithManaAbility("Birds of Paradise"));

        harness.activateAbility(player2, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
    }

    private Permanent addReadyPeacekeeper(Player player, String chosenName) {
        Permanent permanent = addPermanent(player, new AnointedPeacekeeper());
        permanent.setChosenName(chosenName);
        return permanent;
    }

    private Permanent addPermanent(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void preparePlayer2MainPhase() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private static Card createCreatureWithTapAbility(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(CardColor.RED);
        card.setPower(1);
        card.setToughness(1);
        card.addActivatedAbility(new ActivatedAbility(
                true, "{1}", List.of(new DealDamageToAnyTargetEffect(1)),
                "{T}, {1}: " + name + " deals 1 damage to any target."));
        return card;
    }

    private static Card createCreatureWithManaAbility(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(CardColor.GREEN);
        card.setPower(0);
        card.setToughness(1);
        card.addActivatedAbility(new ActivatedAbility(
                true, null, List.of(new AwardAnyColorManaEffect()),
                "{T}: Add one mana of any color."));
        return card;
    }
}
