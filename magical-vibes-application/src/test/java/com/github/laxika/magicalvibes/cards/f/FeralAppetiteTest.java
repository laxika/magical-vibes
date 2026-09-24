package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PestSummoning;
import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FeralAppetite.class, PestSummoning.class, GrizzlyBears.class, Cancel.class, Shock.class})
class FeralAppetiteTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking Pests get +1/+0 and deathtouch")
    void attackingPestsGetBoostAndDeathtouch() {
        harness.addToBattlefield(player1, new FeralAppetite());
        harness.setHand(player1, List.of(new PestSummoning()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        List<Permanent> pests = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Pest"))
                .toList();
        pests.forEach(pest -> pest.setSummoningSick(false));
        pests.getFirst().setAttacking(true);

        assertThat(gqs.getEffectivePower(gd, pests.getFirst())).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, pests.getFirst())).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, pests.getFirst(), Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.getEffectivePower(gd, pests.get(1))).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, pests.get(1), Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Exiling a creature card creates a Pest token")
    void exilingCreatureCreatesPest() {
        harness.addToBattlefield(player1, new FeralAppetite());
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, new ArrayList<>(List.of(creature)));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, creature.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(creature);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Pest")))
                .hasSize(1);
    }

    @Test
    @DisplayName("Exiling a noncreature card creates no Pest")
    void exilingNoncreatureCreatesNoPest() {
        harness.addToBattlefield(player1, new FeralAppetite());
        Card noncreature = new Cancel();
        harness.setGraveyard(player2, new ArrayList<>(List.of(noncreature)));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, noncreature.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player2, "Cancel");
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Pest")))
                .isEmpty();
    }

    @Test
    @DisplayName("The created Pest token gains 1 life when it dies")
    void createdPestGainsLifeWhenItDies() {
        harness.addToBattlefield(player1, new FeralAppetite());
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, new ArrayList<>(List.of(creature)));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, 0, null, creature.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        Permanent pest = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Pest"))
                .findFirst()
                .orElseThrow();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, pest.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }
}
