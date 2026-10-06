package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ChillToTheBone;
import com.github.laxika.magicalvibes.cards.g.GoblinFurrier;
import com.github.laxika.magicalvibes.cards.m.MartyrOfAshes;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SekKuarDeathkeeper.class, GoblinFurrier.class, ChillToTheBone.class, MartyrOfAshes.class})
class SekKuarDeathkeeperTest extends BaseCardTest {

    @Test
    void createsGravebornWhenAnotherNontokenCreatureYouControlDies() {
        harness.addToBattlefield(player1, new SekKuarDeathkeeper());
        harness.addToBattlefield(player1, new GoblinFurrier());

        destroyWithChillToTheBone(player2, player1, "Goblin Furrier");
        resolveAllTriggers();

        Permanent graveborn = findPermanent(player1, "Graveborn");
        assertThat(graveborn.getCard().getPower()).isEqualTo(3);
        assertThat(graveborn.getCard().getToughness()).isEqualTo(1);
        assertThat(graveborn.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(graveborn.getCard().getColors()).containsExactlyInAnyOrder(CardColor.BLACK, CardColor.RED);
        assertThat(graveborn.getCard().getSubtypes()).contains(CardSubtype.GRAVEBORN);
        assertThat(graveborn.getCard().getKeywords()).contains(Keyword.HASTE);
    }

    @Test
    void doesNotTriggerWhenSekKuarDies() {
        harness.addToBattlefield(player1, new SekKuarDeathkeeper());

        destroyWithChillToTheBone(player2, player1, "Sek'Kuar, Deathkeeper");

        assertThat(findPermanents(player1, "Graveborn")).isEmpty();
    }

    @Test
    void doesNotTriggerWhenTokenCreatureYouControlDies() {
        harness.addToBattlefield(player1, new SekKuarDeathkeeper());
        harness.addToBattlefield(player1, new GoblinFurrier());

        destroyWithChillToTheBone(player2, player1, "Goblin Furrier");
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Graveborn")).hasSize(1);

        destroyWithChillToTheBone(player2, player1, "Graveborn");

        assertThat(findPermanents(player1, "Graveborn")).isEmpty();
    }

    @Test
    void doesNotTriggerForAnOpponentsCreature() {
        harness.addToBattlefield(player1, new SekKuarDeathkeeper());
        harness.addToBattlefield(player2, new GoblinFurrier());

        destroyWithChillToTheBone(player1, player2, "Goblin Furrier");

        assertThat(findPermanents(player1, "Graveborn")).isEmpty();
    }

    @Test
    void resolvesExistingTriggerAfterSekKuarDies() {
        harness.addToBattlefield(player1, new SekKuarDeathkeeper());
        harness.addToBattlefield(player1, new GoblinFurrier());

        destroyWithChillToTheBone(player2, player1, "Goblin Furrier");
        assertThat(gd.stack).hasSize(1);
        destroyWithChillToTheBone(player2, player1, "Sek'Kuar, Deathkeeper");
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Sek'Kuar, Deathkeeper");
        assertThat(findPermanents(player1, "Graveborn")).hasSize(1);
        assertThat(findPermanents(player2, "Graveborn")).isEmpty();
    }

    @Test
    void triggersForEachOtherNontokenCreatureDyingSimultaneouslyWithSekKuar() {
        harness.addToBattlefield(player2, new MartyrOfAshes());
        harness.addToBattlefield(player1, new SekKuarDeathkeeper());
        harness.addToBattlefield(player1, new GoblinFurrier());
        harness.addToBattlefield(player1, new GoblinFurrier());
        harness.addToBattlefield(player2, new GoblinFurrier());
        GoblinFurrier first = new GoblinFurrier();
        GoblinFurrier second = new GoblinFurrier();
        GoblinFurrier third = new GoblinFurrier();
        harness.setHand(player2, List.of(first, second, third));
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.activateAbility(player2, 0, 3, null);
        harness.handleMultipleCardsChosen(player2, List.of(first.getId(), second.getId(), third.getId()));
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Sek'Kuar, Deathkeeper");
        harness.assertInGraveyard(player2, "Martyr of Ashes");
        harness.assertNotOnBattlefield(player1, "Goblin Furrier");
        harness.assertNotOnBattlefield(player2, "Goblin Furrier");
        assertThat(findPermanents(player1, "Graveborn")).hasSize(2);
        assertThat(findPermanents(player2, "Graveborn")).isEmpty();
    }

    private void destroyWithChillToTheBone(Player caster, Player targetController, String targetName) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new ChillToTheBone()));
        harness.addMana(caster, ManaColor.COLORLESS, 3);
        harness.addMana(caster, ManaColor.BLACK, 1);

        UUID targetId = harness.getPermanentId(targetController, targetName);
        harness.castAndResolveInstant(caster, 0, targetId);
    }
}
