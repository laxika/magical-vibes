package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenAttachedToDefendingPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "MB1", collectorNumber = "37")
public class CelestineCaveWitch extends Card {

    public CelestineCaveWitch() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateTokenEffect(
                CardType.CREATURE, 2, "Insect", 1, 1, CardColor.BLACK, null,
                List.of(CardSubtype.INSECT), Set.of(), Set.of(), false, false,
                Map.of(), List.of(), false, false, false, 0, Set.of()));
        addEffect(EffectSlot.ON_ATTACK, new MayEffect(
                new SacrificePermanentThenEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.INSECT),
                        new CreateTokenAttachedToDefendingPlayerEffect(curseToken()),
                        "an Insect"),
                "Sacrifice an Insect?"));
    }

    private static CreateTokenEffect curseToken() {
        return new CreateTokenEffect(
                CardType.ENCHANTMENT, 1, "Curse", 0, 0, CardColor.BLACK, null,
                List.of(CardSubtype.AURA, CardSubtype.CURSE), Set.of(), Set.of(), false, false,
                Map.of(EffectSlot.ENCHANTED_PLAYER_UPKEEP_TRIGGERED,
                        new LoseLifeEffect(1, LoseLifeRecipient.ACTIVE_PLAYER)),
                List.of(), false, false, false, 0, Set.<Keyword>of());
    }
}
