package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GivePoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PoisonRecipient;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "5ED", collectorNumber = "397")
public class SerpentGenerator extends Card {

    public SerpentGenerator() {
        // {4}, {T}: Create a 1/1 colorless Snake artifact creature token. It has "Whenever this
        // creature deals damage to a player, that player gets a poison counter."
        Map<EffectSlot, CardEffect> tokenEffects =
                Map.of(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new GivePoisonCountersEffect(1, PoisonRecipient.TARGET_PLAYER));
        CreateTokenEffect snakeToken = new CreateTokenEffect(CardType.CREATURE, 1, "Snake", 1, 1,
                null, null, List.of(CardSubtype.SNAKE), Set.of(), Set.of(CardType.ARTIFACT),
                false, false, tokenEffects, List.of(), false, false, false, 0, Set.of());

        addActivatedAbility(new ActivatedAbility(true, "{4}", List.of(snakeToken),
                "{4}, {T}: Create a 1/1 colorless Snake artifact creature token. It has "
                        + "\"Whenever this creature deals damage to a player, that player gets a poison counter.\""));
    }
}
