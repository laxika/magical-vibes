package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutSourceCardFromExileIntoLibraryNFromTopEffect;
import com.github.laxika.magicalvibes.model.effect.PutSourceCardFromGraveyardIntoLibraryNFromTopEffect;
import com.github.laxika.magicalvibes.model.effect.SelfExiledFromBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WAR", collectorNumber = "16")
@CardRegistration(set = "DRC", collectorNumber = "66")
public class GodEternalOketra extends Card {

    public GodEternalOketra() {
        addTrigger(Triggers.youCastSpell(), new SpellCastTriggerEffect(
                new CardTypePredicate(CardType.CREATURE),
                List.of(new CreateTokenEffect("Zombie Warrior", 4, 4, CardColor.BLACK,
                        List.of(CardSubtype.ZOMBIE, CardSubtype.WARRIOR),
                        Set.of(Keyword.VIGILANCE), Set.of()))
        ));

        addTrigger(Triggers.dies(ObjectRelation.THIS), new MayEffect(
                new PutSourceCardFromGraveyardIntoLibraryNFromTopEffect(2),
                "Put God-Eternal Oketra into its owner's library third from the top?"));
        addTrigger(Triggers.leavesBattlefield(ObjectRelation.THIS), new SelfExiledFromBattlefieldEffect(
                new MayEffect(
                        new PutSourceCardFromExileIntoLibraryNFromTopEffect(2),
                        "Put God-Eternal Oketra into its owner's library third from the top?")));
    }
}
