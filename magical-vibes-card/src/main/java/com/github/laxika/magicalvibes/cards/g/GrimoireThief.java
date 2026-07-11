package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellsNamedLikeCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsOfOpponentLibraryToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import java.util.List;

@CardRegistration(set = "MOR", collectorNumber = "35")
public class GrimoireThief extends Card {

    public GrimoireThief() {
        // Whenever this creature becomes tapped, exile the top three cards of target opponent's
        // library face down. (Two-player: the single opponent is the only legal target.)
        addEffect(EffectSlot.ON_ALLY_PERMANENT_BECOMES_TAPPED, new TriggeringPermanentConditionalEffect(
                new PermanentIsSourceCardPredicate(),
                new ExileTopCardsOfOpponentLibraryToSourceEffect(3)));

        // {U}, Sacrifice this creature: Turn all cards exiled with this creature face up.
        // Counter all spells with those names. (You may look at the exiled cards.)
        addActivatedAbility(new ActivatedAbility(false, "{U}",
                List.of(new SacrificeSelfCost(), new CounterSpellsNamedLikeCardsExiledWithSourceEffect()),
                "{U}, Sacrifice Grimoire Thief: Turn all cards exiled with Grimoire Thief face up. "
                        + "Counter all spells with those names."));
    }
}
