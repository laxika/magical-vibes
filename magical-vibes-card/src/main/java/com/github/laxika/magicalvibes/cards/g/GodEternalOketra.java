package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutSourceCardFromExileIntoLibraryNFromTopEffect;
import com.github.laxika.magicalvibes.model.effect.PutSourceCardFromGraveyardIntoLibraryNFromTopEffect;
import com.github.laxika.magicalvibes.model.effect.SelfExiledFromBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WAR", collectorNumber = "16")
public class GodEternalOketra extends Card {

    public GodEternalOketra() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardTypePredicate(CardType.CREATURE),
                List.of(new CreateTokenEffect("Zombie Warrior", 4, 4, CardColor.BLACK,
                        List.of(CardSubtype.ZOMBIE, CardSubtype.WARRIOR),
                        Set.of(Keyword.VIGILANCE), Set.of()))
        ));

        addEffect(EffectSlot.ON_DEATH, new MayEffect(
                new PutSourceCardFromGraveyardIntoLibraryNFromTopEffect(2),
                "Put God-Eternal Oketra into its owner's library third from the top?"));
        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD, new SelfExiledFromBattlefieldEffect(
                new MayEffect(
                        new PutSourceCardFromExileIntoLibraryNFromTopEffect(2),
                        "Put God-Eternal Oketra into its owner's library third from the top?")));
    }
}
