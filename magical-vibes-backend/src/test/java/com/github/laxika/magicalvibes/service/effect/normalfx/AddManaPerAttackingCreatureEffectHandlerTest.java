package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AddManaPerAttackingCreatureEffect;
import com.github.laxika.magicalvibes.networking.message.ChooseFromListMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AddManaPerAttackingCreatureEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
            @DisplayName("Sends color choice when attackers exist")
            void sendsColorChoiceWithAttackers() {
                Card card = createCard("Druids' Repository");
                AddManaPerAttackingCreatureEffect effect = new AddManaPerAttackingCreatureEffect(ManaColor.RED, ManaColor.GREEN);
                // xValue = attacker count at trigger time
                StackEntry entry = createEntryWithXValue(card, player1Id, List.of(effect), 3);

                resolveEffect(gd, entry, effect);

                verify(sessionManager).sendToPlayer(eq(player1Id), any(ChooseFromListMessage.class));
            }

            @Test
            @DisplayName("Does nothing when xValue (attacker count) is 0")
            void doesNothingWithZeroAttackers() {
                Card card = createCard("Druids' Repository");
                AddManaPerAttackingCreatureEffect effect = new AddManaPerAttackingCreatureEffect(ManaColor.RED, ManaColor.GREEN);
                StackEntry entry = createEntryWithXValue(card, player1Id, List.of(effect), 0);

                resolveEffect(gd, entry, effect);

                verify(sessionManager, never()).sendToPlayer(any(), any(ChooseFromListMessage.class));
            }
}
