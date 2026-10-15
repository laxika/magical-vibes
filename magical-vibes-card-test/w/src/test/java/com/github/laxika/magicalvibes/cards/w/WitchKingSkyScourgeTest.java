package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LordOfTheNazgL;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
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
}
