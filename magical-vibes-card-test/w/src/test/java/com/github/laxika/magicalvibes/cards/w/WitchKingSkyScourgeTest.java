package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LordOfTheNazgL;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WitchKingSkyScourge.class, LordOfTheNazgL.class, GrizzlyBears.class, Mountain.class})
class WitchKingSkyScourgeTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles cards equal to the total power of attacking Wraiths")
    void exilesCardsEqualToAttackingWraithsPower() {
        Permanent witchKing = addCreatureReady(player1, new WitchKingSkyScourge());
        Permanent wraith = addCreatureReady(player1, new LordOfTheNazgL());
        addCreatureReady(player1, new GrizzlyBears());

        int attackingWraithPower = gqs.getEffectivePower(gd, witchKing) + gqs.getEffectivePower(gd, wraith);
        List<Card> library = new ArrayList<>();
        for (int i = 0; i < attackingWraithPower + 2; i++) {
            library.add(new Mountain());
        }
        harness.setLibrary(player1, library);

        declareAttackers(List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(witchKing),
                gd.playerBattlefields.get(player1.getId()).indexOf(wraith)));
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyElementsOf(library.subList(0, attackingWraithPower));
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyElementsOf(library.subList(attackingWraithPower, library.size()));
        assertThat(gd.exilePlayPermissions).containsEntry(library.getFirst().getId(), player1.getId());
    }

    @Test
    @DisplayName("Doesn't trigger when no Wraith attacks")
    void doesNotTriggerWhenNoWraithAttacks() {
        addCreatureReady(player1, new WitchKingSkyScourge());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        List<Card> library = new ArrayList<>(List.of(new Mountain(), new Mountain()));
        harness.setLibrary(player1, library);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () ->
                declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(bears))));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A Wraith that left the battlefield counts with its last-known power")
    void wraithThatLeftCountsWithItsLastKnownPower() {
        Permanent witchKing = addCreatureReady(player1, new WitchKingSkyScourge());
        Permanent wraith = addCreatureReady(player1, new LordOfTheNazgL());

        int attackingWraithPower = gqs.getEffectivePower(gd, witchKing) + gqs.getEffectivePower(gd, wraith);
        List<Card> library = new ArrayList<>();
        for (int i = 0; i < attackingWraithPower + 2; i++) {
            library.add(new Mountain());
        }
        harness.setLibrary(player1, library);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(
                    gd.playerBattlefields.get(player1.getId()).indexOf(witchKing),
                    gd.playerBattlefields.get(player1.getId()).indexOf(wraith)));
            gd.playerBattlefields.get(player1.getId()).remove(wraith);
            harness.passBothPriorities();
        });

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyElementsOf(library.subList(0, attackingWraithPower));
    }
}
