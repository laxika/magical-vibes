package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.ReturnPermanentsOnCombatDamageToPlayerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

class ReturnPermanentsOnCombatDamageToPlayerEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
            @DisplayName("Begins multi-permanent choice when defender has valid permanents")
            void beginsMultiPermanentChoice() {
                Card card = createCard("Ninja of the Deep Hours");
                ReturnPermanentsOnCombatDamageToPlayerEffect effect = new ReturnPermanentsOnCombatDamageToPlayerEffect();
                // controllerId=attacker, targetId=defender, xValue=damage dealt
                StackEntry entry = createEntryWithXValueAndTarget(card, player1Id, List.of(effect), 3, player2Id);

                Permanent perm1 = new Permanent(createCard("Grizzly Bears"));
                Permanent perm2 = new Permanent(createCard("Hill Giant"));
                gd.playerBattlefields.get(player2Id).add(perm1);
                gd.playerBattlefields.get(player2Id).add(perm2);

                resolveEffect(gd, entry, effect);

                assertThat(gd.pendingCombatDamageBounceTargetPlayerId).isEqualTo(player2Id);
                verify(playerInputService).beginMultiPermanentChoice(eq(gd), eq(player1Id), any(), eq(2), any());
            }

            @Test
            @DisplayName("Logs and does nothing when defender has no permanents")
            void noDefenderPermanents() {
                Card card = createCard("Ninja of the Deep Hours");
                ReturnPermanentsOnCombatDamageToPlayerEffect effect = new ReturnPermanentsOnCombatDamageToPlayerEffect();
                StackEntry entry = createEntryWithXValueAndTarget(card, player1Id, List.of(effect), 2, player2Id);

                resolveEffect(gd, entry, effect);

                verify(playerInputService, never()).beginMultiPermanentChoice(any(), any(), any(), any(int.class), any());
                verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                        msg.contains("no permanents")));
            }

            @Test
            @DisplayName("Filters by predicate when filter is set")
            void filtersByPredicate() {
                Card card = createCard("Scalpelexis");
                PermanentPredicate filter = new PermanentTruePredicate();
                ReturnPermanentsOnCombatDamageToPlayerEffect effect = new ReturnPermanentsOnCombatDamageToPlayerEffect(filter);
                StackEntry entry = createEntryWithXValueAndTarget(card, player1Id, List.of(effect), 2, player2Id);

                Permanent matching = new Permanent(createCard("Grizzly Bears"));
                Permanent nonMatching = new Permanent(createCard("Mountain"));
                gd.playerBattlefields.get(player2Id).add(matching);
                gd.playerBattlefields.get(player2Id).add(nonMatching);

                when(predicateEvaluationService.matchesPermanentPredicate(gd, matching, filter)).thenReturn(true);
                when(predicateEvaluationService.matchesPermanentPredicate(gd, nonMatching, filter)).thenReturn(false);

                resolveEffect(gd, entry, effect);

                verify(playerInputService).beginMultiPermanentChoice(eq(gd), eq(player1Id),
                        argThat(ids -> ids.size() == 1 && ids.contains(matching.getId())), eq(1), any());
            }
}
