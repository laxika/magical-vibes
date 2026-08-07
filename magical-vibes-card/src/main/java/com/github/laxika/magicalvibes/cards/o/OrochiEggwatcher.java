package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.s.ShidakoBroodmistress;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.TransformToBackFaceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "CHK", collectorNumber = "233")
public class OrochiEggwatcher extends Card {

    public OrochiEggwatcher() {
        setBackFaceCard(new ShidakoBroodmistress());

        // "{2}{G}, {T}: Create a 1/1 green Snake creature token. If you control ten or more creatures,
        // flip this creature." - the creature count is checked after the token is made, so the token
        // itself counts toward the ten.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{G}",
                List.of(
                        new CreateTokenEffect("Snake", 1, 1, CardColor.GREEN, List.of(CardSubtype.SNAKE),
                                Set.of(), Set.of()),
                        new ConditionalEffect(
                                new ControlsPermanentCount(10, new PermanentIsCreaturePredicate()),
                                new TransformToBackFaceEffect()
                        )
                ),
                "{2}{G}, {T}: Create a 1/1 green Snake creature token. If you control ten or more "
                        + "creatures, flip Orochi Eggwatcher."
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "ShidakoBroodmistress";
    }
}
